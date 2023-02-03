-----------------------create-new-tables-for-faceted-search-by-products---------------------
CREATE TABLE ProductIntegerIndexes (
    userId UUID NOT NULL,
    productIndexes VARBIT NOT NULL,
    FOREIGN KEY(userId) REFERENCES Users(userId) ON DELETE CASCADE ON UPDATE CASCADE,
    PRIMARY KEY(userId)
);

CREATE TABLE ProductFiltering (
    userId UUID NOT NULL,
    filterValue VARCHAR(256) NOT NULL,
    filterType VARCHAR(128) NOT NULL,
    productIndexes VARBIT NOT NULL
    FOREIGN KEY(userId) REFERENCES Users(userId) ON DELETE CASCADE ON UPDATE CASCADE,
    PRIMARY KEY(userId, filterValue, filterType)
);

------------------------add-new-column-to-Product-table-and-migrate-data--------------------------
ALTER TABLE Products ADD COLUMN integerIndex INT;
DO
$$
  DECLARE temp_row record;
  DECLARE rows_number record;
BEGIN
    FOR temp_row IN SELECT userId FROM Products GROUP BY userId
	LOOP
		CREATE SEQUENCE productIndexesSequence minvalue 0 start with 0;
		UPDATE Products SET integerIndex = nextval('productIndexesSequence')
		     WHERE userId = temp_row.userId;
		SELECT last_value INTO rows_number FROM productIndexesSequence;
		INSERT INTO ProductIntegerIndexes(userId, productIndexes)
		     VALUES ( temp_row.userId, ~(0::bit(1000)::varbit) >> (999 - rows_number.last_value::int) );
		DROP SEQUENCE productIndexesSequence;
	END LOOP;
END
$$;
ALTER TABLE Products ADD CONSTRAINT uniqueIntegerIndexes UNIQUE(userId, integerIndex);
ALTER TABLE Products ALTER COLUMN integerIndex SET NOT NULL;

DO
$$
  DECLARE temp_row record;
BEGIN
    FOR temp_row IN SELECT * FROM Products
	LOOP
		INSERT INTO ProductFiltering(userId, filterValue, filterType, productIndexes)
		    VALUES (temp_row.userId, temp_row.category, 'category', 0::bit(1000)::varbit),
		    (temp_row.userId, temp_row.shop, 'shop', 0::bit(1000)::varbit),
		    (temp_row.userId, temp_row.grade, 'grade', 0::bit(1000)::varbit),
		    (temp_row.userId, temp_row.manufacturer, 'manufacturer', 0::bit(1000)::varbit)
		    ON CONFLICT DO NOTHING;
		UPDATE ProductFiltering
		    SET productIndexes = set_bit(productIndexes, 999 - temp_row.integerIndex, 1)
		    WHERE userId = temp_row.userId AND filterValue = temp_row.category AND filterType = 'category';
		UPDATE ProductFiltering
		    SET productIndexes = set_bit(productIndexes, 999 - temp_row.integerIndex, 1)
		    WHERE userId = temp_row.userId AND filterValue = temp_row.shop AND filterType = 'shop';
		UPDATE ProductFiltering
            SET productIndexes = set_bit(productIndexes, 999 - temp_row.integerIndex, 1)
            WHERE userId = temp_row.userId AND filterValue = temp_row.grade AND filterType = 'grade';
         UPDATE ProductFiltering
             SET productIndexes = set_bit(productIndexes, 999 - temp_row.integerIndex, 1)
             WHERE userId = temp_row.userId AND filterValue = temp_row.manufacturer AND filterType = 'manufacturer';
	END LOOP;

	FOR temp_row IN SELECT * FROM ProductTags INNER JOIN Products ON ProductTags.productId = Products.productId
	LOOP
	    INSERT INTO ProductFiltering(userId, filterValue, filterType, productIndexes)
	        VALUES (temp_row.userId, temp_row.tagValue, 'tag', 0::bit(1000)::varbit)
	        ON CONFLICT DO NOTHING;
        UPDATE ProductFiltering
            SET productIndexes = set_bit(productIndexes, 999 - temp_row.integerIndex, 1)
            WHERE userId = temp_row.userId AND filterValue = temp_row.tagValue AND filterType = 'tag';
	END LOOP;
END
$$;

---------------------------create-triggers-for-add-new-product--------------------------------------
CREATE FUNCTION addNewProductToProductFiltering() returns trigger as $$
    DECLARE bitsNumber INT;
            filterRow record;
    BEGIN
        SELECT bit_length(productIndexes) INTO bitsNumber
            FROM ProductIntegerIndexes
            WHERE ProductIntegerIndexes.userId = NEW.userId;
        IF bitsNumber IS NULL THEN
            bitsNumber := (NEW.integerIndex / 1000 + 1) * 1000;
            INSERT INTO ProductIntegerIndexes(userId, productIndexes)
                VALUES (NEW.userId, 1::bit(1000)::varbit);
        ELSIF bitsNumber < (NEW.integerIndex / 1000 + 1) * 1000 THEN
            bitsNumber := (NEW.integerIndex / 1000 + 1) * 1000;
            UPDATE ProductIntegerIndexes
                SET productIndexes = set_bit((lpad('0', 1000, '0')::varbit) || productIndexes,
                                             bitsNumber - 1 - NEW.integerIndex,
                                             1)
                WHERE ProductIntegerIndexes.userId = NEW.userId;
            UPDATE ProductFiltering
                SET productIndexes = (lpad('0', 1000, '0')::varbit) || productIndexes
                WHERE ProductFiltering.userId = NEW.userId;
        END IF;

        INSERT INTO ProductFiltering(userId, filterValue, filterType, productIndexes)
            VALUES (NEW.userId, NEW.category, 'category', lpad('0', bitsNumber, '0')::varbit),
            (NEW.userId, NEW.shop, 'shop', lpad('0', bitsNumber, '0')::varbit),
            (NEW.userId, NEW.grade, 'grade', lpad('0', bitsNumber, '0'))::varbit),
            (NEW.userId, NEW.manufacturer, 'manufacturer', lpad('0', bitsNumber, '0')::varbit)
            ON CONFLICT DO NOTHING;

        UPDATE ProductFiltering
            SET productIndexes = set_bit(productIndexes, bitsNumber - 1 - NEW.integerIndex, 1)
            WHERE userId = NEW.userId AND filterValue = NEW.category AND filterType = 'category';
        UPDATE ProductFiltering
            SET productIndexes = set_bit(productIndexes, bitsNumber - 1 - NEW.integerIndex, 1)
            WHERE userId = NEW.userId AND filterValue = NEW.shop AND filterType = 'shop';
        UPDATE ProductFiltering
            SET productIndexes = set_bit(productIndexes, bitsNumber - 1 - NEW.integerIndex, 1)
            WHERE userId = NEW.userId AND filterValue = NEW.grade AND filterType = 'grade';
        UPDATE ProductFiltering
            SET productIndexes = set_bit(productIndexes, bitsNumber - 1 - NEW.integerIndex, 1)
            WHERE userId = NEW.userId AND filterValue = NEW.manufacturer AND filterType = 'manufacturer';

        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION addNewProductTagToProductFiltering() returns trigger as $$
    DECLARE product_row record;
            bitsNumber INT;
    BEGIN
        SELECT * INTO product_row
            FROM Products
            INNER JOIN Products ON ProductTags.productId = Products.productId
            WHERE ProductTags.productId = NEW.productId AND ProductTags.tagValue = NEW.tagValue;
        SELECT bit_length(productIndexes) INTO bitsNumber
            FROM ProductIntegerIndexes
            WHERE ProductIntegerIndexes.userId = NEW.userId;

        INSERT INTO ProductFiltering(userId, filterValue, filterType, productIndexes)
            VALUES (product_row.userId, product_row.tagValue, 'tag', lpad('0', bitsNumber, '0')::varbit)
            ON CONFLICT DO NOTHING;

        UPDATE ProductFiltering
            SET productIndexes = set_bit(productIndexes, bitsNumber - 1 - product_row.integerIndex, 1)
            WHERE userId = product_row.userId AND filterValue = product_row.tagValue AND filterType = 'tag';

        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER addNewProductToProductFilteringTrigger AFTER INSERT ON Products
    FOR EACH ROW EXECUTE addNewProductToProductFiltering();
CREATE TRIGGER addNewProductTagToProductFilteringTrigger AFTER INSERT ON ProductTags
    FOR EACH ROW EXECUTE addNewProductTagToProductFiltering();

---------------------------create-triggers-for-delete-product---------------------------------------
CREATE FUNCTION deleteProductFromProductFiltering() returns trigger as $$
    DECLARE bitsNumber INT;
    BEGIN
        SELECT bit_length(productIndexes) INTO bitsNumber
            FROM ProductIntegerIndexes
            WHERE ProductIntegerIndexes.userId = OLD.userId;

        UPDATE ProductIntegerIndexes
            SET productIndexes = set_bit(productIndexes, bitsNumber - 1 - OLD.integerIndex, 0)
            WHERE userId = OLD.userId;

        UPDATE ProductFiltering
            SET productIndexes = set_bit(productIndexes, bitsNumber - 1 - OLD.integerIndex, 0)
            WHERE userId = OLD.userId;

        RETURN OLD;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER deleteProductFromProductFilteringTrigger AFTER DELETE ON Products
    FOR EACH ROW EXECUTE deleteProductFromProductFiltering();

---------------------------create-triggers-for-update-product---------------------------------------
CREATE FUNCTION updateProductInProductFiltering() returns trigger as $$
    DECLARE bitsNumber INT;
    BEGIN
        SELECT bit_length(productIndexes) INTO bitsNumber
            FROM ProductFiltering
            WHERE ProductFiltering.userId = OLD.userId;

        UPDATE ProductFiltering
            SET productIndexes = set_bit(productIndexes, bitsNumber - 1 - OLD.integerIndex, 0)
            WHERE userId = OLD.userId;

        INSERT INTO ProductFiltering(userId, filterValue, filterType, productIndexes)
            VALUES (NEW.userId, NEW.category, 'category', lpad('0', bitsNumber, '0')::varbit),
            (NEW.userId, NEW.shop, 'shop', lpad('0', bitsNumber, '0')::varbit),
            (NEW.userId, NEW.grade, 'grade', lpad('0', bitsNumber, '0'))::varbit),
            (NEW.userId, NEW.manufacturer, 'manufacturer', lpad('0', bitsNumber, '0')::varbit)
            ON CONFLICT DO NOTHING;

        UPDATE ProductFiltering
            SET productIndexes = set_bit(productIndexes, bitsNumber - 1 - NEW.integerIndex, 1)
            WHERE userId = NEW.userId AND filterValue = NEW.category AND filterType = 'category';
        UPDATE ProductFiltering
            SET productIndexes = set_bit(productIndexes, bitsNumber - 1 - NEW.integerIndex, 1)
            WHERE userId = NEW.userId AND filterValue = NEW.shop AND filterType = 'shop';
        UPDATE ProductFiltering
            SET productIndexes = set_bit(productIndexes, bitsNumber - 1 - NEW.integerIndex, 1)
            WHERE userId = NEW.userId AND filterValue = NEW.grade AND filterType = 'grade';
        UPDATE ProductFiltering
            SET productIndexes = set_bit(productIndexes, bitsNumber - 1 - NEW.integerIndex, 1)
            WHERE userId = NEW.userId AND filterValue = NEW.manufacturer AND filterType = 'manufacturer';

        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION updateProductTagInProductFiltering() returns trigger as $$
    DECLARE product_row record;
            bitsNumber INT;
    BEGIN
        SELECT * INTO product_row
            FROM Products
            INNER JOIN Products ON ProductTags.productId = Products.productId
            WHERE ProductTags.productId = NEW.productId AND ProductTags.tagValue = NEW.tagValue;
        SELECT bit_length(productIndexes) INTO bitsNumber
            FROM ProductIntegerIndexes
            WHERE ProductIntegerIndexes.userId = NEW.userId;

        INSERT INTO ProductFiltering(userId, filterValue, filterType, productIndexes)
            VALUES (product_row.userId, product_row.tagValue, 'tag', lpad('0', bitsNumber, '0')::varbit)
            ON CONFLICT DO NOTHING;

        UPDATE ProductFiltering
            SET productIndexes = set_bit(productIndexes, bitsNumber - 1 - product_row.integerIndex, 1)
            WHERE userId = product_row.userId AND filterValue = product_row.tagValue AND filterType = 'tag';

        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER updateProductInProductFilteringTrigger AFTER UPDATE ON Products
    FOR EACH ROW EXECUTE updateProductInProductFiltering();
CREATE TRIGGER updateProductTagInProductFilteringTrigger AFTER UPDATE ON ProductTags
    FOR EACH ROW EXECUTE updateProductTagInProductFiltering();