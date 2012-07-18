CREATE TABLE StockPrize (
	id INT,
        symbol VARCHAR(100),
        last DOUBLE,
	chnge FLOAT
 );
 
INSERT INTO StockPrize 
VALUES (1, "IBM" ,11.0	,0.01), (2, "GOOGLE",12.0,0.10), (3, "MICROSOFT",10.5,0.30);


