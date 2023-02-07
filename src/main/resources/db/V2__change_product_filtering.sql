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
    productIndexes VARBIT NOT NULL,
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
CREATE FUNCTION generateProductIntegerIndex() returns trigger AS $$
    DECLARE bits VARBIT;
            bitsNumber INT;
            productIndex INT;
    BEGIN
        SELECT ProductIntegerIndexes.productIndexes INTO bits
            FROM ProductIntegerIndexes
            WHERE ProductIntegerIndexes.userId = NEW.userId;

        IF bits IS NULL THEN
            INSERT INTO ProductIntegerIndexes(userId, productIndexes)
                VALUES (NEW.userId, 0::bit(1000)::varbit);
            bitsNumber := 1000;
        ELSE
            bitsNumber := bit_length(bits);
        END IF;

        productIndex := 0;
        WHILE productIndex < bitsNumber AND get_bit(bits, bitsNumber - 1 - productIndex) = 1 LOOP
            productIndex := productIndex + 1;
        END LOOP;
        NEW.integerIndex := productIndex;

        IF productIndex = bitsNumber THEN
            bitsNumber := (productIndex / 1000 + 1) * 1000;
            UPDATE ProductIntegerIndexes
                SET productIndexes = (lpad('0', 1000, '0')::varbit) || productIndexes
                WHERE ProductIntegerIndexes.userId = NEW.userId;
        END IF;

        UPDATE ProductIntegerIndexes
            SET productIndexes = set_bit(productIndexes, bitsNumber - 1 - productIndex, 1)
            WHERE ProductIntegerIndexes.userId = NEW.userId;

        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION addNewProductToProductFiltering() returns trigger AS $$
    DECLARE bitsNumber INT;
            filterRow record;
    BEGIN
        SELECT bit_length(productIndexes) INTO bitsNumber
            FROM ProductIntegerIndexes
            WHERE ProductIntegerIndexes.userId = NEW.userId;

        INSERT INTO ProductFiltering(userId, filterValue, filterType, productIndexes)
            VALUES (NEW.userId, NEW.category, 'category', lpad('0', bitsNumber, '0')::varbit),
            (NEW.userId, NEW.shop, 'shop', lpad('0', bitsNumber, '0')::varbit),
            (NEW.userId, NEW.grade, 'grade', lpad('0', bitsNumber, '0')::varbit),
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

CREATE FUNCTION addNewProductTagToProductFiltering() returns trigger AS $$
    DECLARE product_row record;
            bitsNumber INT;
    BEGIN
        SELECT * INTO product_row
            FROM Products
            INNER JOIN ProductTags ON Products.productId = ProductTags.productId
            WHERE ProductTags.productId = NEW.productId AND ProductTags.tagValue = NEW.tagValue;
        SELECT bit_length(productIndexes) INTO bitsNumber
            FROM ProductIntegerIndexes
            WHERE ProductIntegerIndexes.userId = product_row.userId;

        INSERT INTO ProductFiltering(userId, filterValue, filterType, productIndexes)
            VALUES (product_row.userId, product_row.tagValue, 'tag', lpad('0', bitsNumber, '0')::varbit)
            ON CONFLICT DO NOTHING;

        UPDATE ProductFiltering
            SET productIndexes = set_bit(productIndexes, bitsNumber - 1 - product_row.integerIndex, 1)
            WHERE userId = product_row.userId AND filterValue = product_row.tagValue AND filterType = 'tag';

        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER generateProductIntegerIndexTrigger BEFORE INSERT ON Products
    FOR EACH ROW EXECUTE FUNCTION generateProductIntegerIndex();
CREATE TRIGGER addNewProductToProductFilteringTrigger AFTER INSERT ON Products
    FOR EACH ROW EXECUTE FUNCTION addNewProductToProductFiltering();
CREATE TRIGGER addNewProductTagToProductFilteringTrigger AFTER INSERT ON ProductTags
    FOR EACH ROW EXECUTE FUNCTION addNewProductTagToProductFiltering();

---------------------------create-triggers-for-delete-product---------------------------------------
CREATE FUNCTION deleteProductFromProductFiltering() returns trigger AS $$
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
    FOR EACH ROW EXECUTE FUNCTION deleteProductFromProductFiltering();

---------------------------create-triggers-for-update-product---------------------------------------
CREATE FUNCTION updateProductInProductFiltering() returns trigger AS $$
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
            (NEW.userId, NEW.grade, 'grade', lpad('0', bitsNumber, '0')::varbit),
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

CREATE FUNCTION updateProductTagInProductFiltering() returns trigger AS $$
    DECLARE product_row record;
            bitsNumber INT;
    BEGIN
        SELECT * INTO product_row
            FROM Products
            INNER JOIN ProductTags ON Products.productId = ProductTags.productId
            WHERE ProductTags.productId = NEW.productId AND ProductTags.tagValue = NEW.tagValue;
        SELECT bit_length(productIndexes) INTO bitsNumber
            FROM ProductIntegerIndexes
            WHERE ProductIntegerIndexes.userId = product_row.userId;

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
    FOR EACH ROW EXECUTE FUNCTION updateProductInProductFiltering();
CREATE TRIGGER updateProductTagInProductFilteringTrigger AFTER UPDATE ON ProductTags
    FOR EACH ROW EXECUTE FUNCTION updateProductTagInProductFiltering();