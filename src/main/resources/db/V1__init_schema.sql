CREATE TABLE Users (
    userId UUID NOT NULL,
    name VARCHAR(64) NOT NULL,
    passwordHash VARCHAR(512) NOT NULL,
    email VARCHAR(512) NOT NULL,
    salt VARCHAR(512) NOT NULL,
    PRIMARY KEY(userId),
    UNIQUE(name),
    UNIQUE(email),
    UNIQUE(salt)
);

CREATE TABLE Roles (
    roleId SERIAL NOT NULL,
    name VARCHAR(64) NOT NULL,
    PRIMARY KEY(roleId),
    UNIQUE(name)
);

CREATE TABLE UsersToRoles (
    userId UUID NOT NULL,
    roleId INT NOT NULL,
    FOREIGN KEY(userId) REFERENCES Users(userId) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY(roleId) REFERENCES Roles(roleId) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE Products (
    productId UUID NOT NULL,
    userId UUID NOT NULL,
    category VARCHAR(256) NOT NULL,
    shop VARCHAR(256) NOT NULL,
    grade VARCHAR(256) NOT NULL,
    manufacturer VARCHAR(256) NOT NULL,
    description TEXT,
    imagePath VARCHAR(512),
    quantity NUMERIC(16, 6) NOT NULL,
    unit VARCHAR(256) NOT NULL,
    price NUMERIC(16, 6) NOT NULL,
    packingSize NUMERIC(16, 6) NOT NULL,
    contextHash VARCHAR(512) NOT NULL,
    FOREIGN KEY(userId) REFERENCES Users(userId) ON DELETE CASCADE ON UPDATE CASCADE,
    PRIMARY KEY(productId),
    UNIQUE(productId, userId),
    UNIQUE(contextHash, userId)
);

CREATE TABLE Dishes (
    dishId UUID NOT NULL,
    userId UUID NOT NULL,
    name VARCHAR(256) NOT NULL,
    servingSize NUMERIC(16, 6) NOT NULL,
    unit VARCHAR(256) NOT NULL,
    description TEXT,
    imagePath VARCHAR(512),
    FOREIGN KEY(userId) REFERENCES Users(userId) ON DELETE CASCADE ON UPDATE CASCADE,
    PRIMARY KEY(dishId),
    UNIQUE(dishId, userId),
    UNIQUE(name, userId)
);

CREATE TABLE Menus (
    menuId UUID NOT NULL,
    userId UUID NOT NULL,
    name VARCHAR(256) NOT NULL,
    description TEXT,
    imagePath VARCHAR(512),
    FOREIGN KEY(userId) REFERENCES Users(userId) ON DELETE CASCADE ON UPDATE CASCADE,
    PRIMARY KEY(menuId),
    UNIQUE(menuId, userId),
    UNIQUE(name, userId)
);

CREATE TABLE MenusToDishes (
    dishId UUID NOT NULL,
    menuId UUID NOT NULL,
    quantity NUMERIC(16, 6) NOT NULL,
    FOREIGN KEY(dishId) REFERENCES Dishes(dishId) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY(menuId) REFERENCES Menus(menuId) ON DELETE CASCADE ON UPDATE CASCADE,
    PRIMARY KEY(dishId, menuId)
);

CREATE TABLE ProductTags (
    productId UUID NOT NULL,
    tagValue VARCHAR(256) NOT NULL,
    index INT NOT NULL,
    FOREIGN KEY(productId) REFERENCES Products(productId) ON DELETE CASCADE ON UPDATE CASCADE,
    PRIMARY KEY(productId, tagValue)
);

CREATE TABLE DishTags (
    dishId UUID NOT NULL,
    tagValue VARCHAR(256) NOT NULL,
    index INT NOT NULL,
    FOREIGN KEY(dishId) REFERENCES Dishes(dishId) ON DELETE CASCADE ON UPDATE CASCADE,
    PRIMARY KEY(dishId, tagValue)
);

CREATE TABLE DishIngredients (
    dishId UUID NOT NULL,
    name VARCHAR(256) NOT NULL,
    quantity NUMERIC(16, 6) NOT NULL,
    filter JSONB NOT NULL,
    filterQuery VARCHAR(2096) NOT NULL,
    index INT NOT NULL,
    FOREIGN KEY(dishId) REFERENCES Dishes(dishId) ON DELETE CASCADE ON UPDATE CASCADE,
    PRIMARY KEY(dishId, name)
);

CREATE TABLE JwsBlackList (
    tokenId UUID NOT NULL,
    expiration TIMESTAMP NOT NULL,
    PRIMARY KEY(tokenId)
);

CREATE TABLE UsedImages (
    userId UUID NOT NULL,
    imageHash VARCHAR(512) NOT NULL,
    imageUrl VARCHAR(512) NOT NULL,
    FOREIGN KEY(userId) REFERENCES Users(userId) ON DELETE CASCADE ON UPDATE CASCADE,
    PRIMARY KEY(userId, imageHash)
);

CREATE FUNCTION existProductsForFilter(productCategories VARCHAR(256)[], filterQuery VARCHAR(2048))
    RETURNS BOOLEAN
	LANGUAGE plpgsql
    AS $BODY$
DECLARE
	res BOOLEAN;
BEGIN
	EXECUTE format(
		'SELECT EXISTS (
        	SELECT * FROM (%s) AS P
            	WHERE P.category = ANY(''%s'')
     	);',
		filterQuery,
		productCategories
	) INTO res;
	RETURN res;
END;
$BODY$;