CREATE TABLE Users (
    userId UUID NOT NULL,
    name VARCHAR(64) NOT NULL,
    passwordHash VARCHAR(512) NOT NULL,
    emailHash VARCHAR(512) NOT NULL,
    salt VARCHAR(512) NOT NULL,
    PRIMARY KEY(userId),
    UNIQUE(name),
    UNIQUE(passwordHash),
    UNIQUE(emailHash),
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
    variety VARCHAR(256) NOT NULL,
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
    FOREIGN KEY(productId) REFERENCES Products(productId) ON DELETE CASCADE ON UPDATE CASCADE,
    PRIMARY KEY(productId, tagValue)
);

CREATE TABLE DishTags (
    dishId UUID NOT NULL,
    tagValue VARCHAR(256) NOT NULL,
    FOREIGN KEY(dishId) REFERENCES Dishes(dishId) ON DELETE CASCADE ON UPDATE CASCADE,
    PRIMARY KEY(dishId, tagValue)
);

CREATE TABLE FilterGroups (
    dishId UUID NOT NULL,
    dishTagFilters JSONB NOT NULL,
    quantity NUMERIC(16, 6) NOT NULL,
    FOREIGN KEY(dishId) REFERENCES Dishes(dishId) ON DELETE CASCADE ON UPDATE CASCADE
);