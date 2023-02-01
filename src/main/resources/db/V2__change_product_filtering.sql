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

ALTER TABLE Products ADD COLUMN integerIndex INT;
ALTER TABLE Products ADD CONSTRAINT uniqueIntegerIndexes UNIQUE(userId, integerIndex);
DO
$$declare temp_row record;
BEGIN
    FOR temp_row IN SELECT userId FROM Products GROUP BY userId
	LOOP
		CREATE SEQUENCE productIndexesSequence minvalue 0 start with 0;
		update Products set integerIndex = nextval('productIndexesSequence')
		     where userId = temp_row.userId;
		DROP SEQUENCE productIndexesSequence;
	END LOOP;
END$$;
ALTER TABLE Products ALTER COLUMN integerIndex SET NOT NULL;

