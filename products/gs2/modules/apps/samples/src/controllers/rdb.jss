function doGet(request, response, session) {
    var db = new RDB("mysql", "localhost", "3306", "myDB", "root", "root");
	db.query("SELECT * FROM myTable", function(results) {
		var path = "/home/ruchira/Desktop/";
		var file = new File(path + "rdbTest.txt");
		if(!file.exists) file.createFile();
		file.openForAppending();
		for(var i=0;i<results.length;i++) {
			file.writeLine(results[i].myColumn);
		}
		file.close();
	});
    log("Database query executed.", "info");
	response.write("<html><body><h1>Done</h1></body></html>");
}

/*
	=====================================================================
	RDB API
	=====================================================================

	//connecting to the database. Replace "mysql" with "mssql".. etc. with the proper
	//driver installed.
    var db = new RDB("mysql", "localhost", "3310", "myDB", "username", "password");

	//simple query with a callback function
    db.query("SELECT * FROM table", function() {

    });

	//a query with wildcards. Values for ? can be specified as arguments 
	//of the function. You can have multiple wildcard characters
    var results = db.query("SELECT * FROM table WHERE id=? and name='?'", 1, "ruchira", function() {

    });

	//a query with wildcards and callback functions
    db.query("SELECT * FROM table WHERE key = ?", key, function(results) {
        if (results.length > 0) {
            for (var i = 0; i < results.length; i++) {
                var row = results[i]
            }
        }
    });

	//usual queries
    var query = "SELECT * FROM table WHERE key = ? and value = ?";
    var key = 1;
    var value = "value";
    var results = db.query(query, key);

    if (results.length > 0) {
        for (var i = 0; i < results.length; i++) {
            var row = results[i]
        }
    }


	//batch request
    var queries = [
        "INSERT INTO table (?, ?)",
        "UPDATE table SET value = ? WHERE key = 1"
    ];

    var values = [
        ['ruchira', 'wageesha'],
        ['jaggery']
    ];
    db.query(queries, values, function(array) {
		//@array array of affected rows counts
    });


	//transaction
    db.autoCommit = false;
    db.query(query, value1, value2);
    db.query(query, value1);

    var sp = db.savePoint("s1");
    // or var sp = db.savePoint();

    db.releasePoint("s1");

    db.rollback("s1");//db.rollback(sp);


    db.commit();
    if (db.error) {
        db.rollback();
    }
    db.commit(function(db) {
        if (db.error) {
            db.rollback();
        }
        db.autoCommit = true;
    });

}*/

