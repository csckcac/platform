CREATE TABLE IF NOT EXISTS WA_BUILD_HISTORY(WA_BUILD_NUMBER INT NOT NULL AUTO_INCREMENT,
                                             PRIMARY KEY(WA_BUILD_NUMBER));

CREATE TABLE IF NOT EXISTS WA_TEST_SUITE_DETAIL(WA_BUILD_NUMBER integer,
                                                  FOREIGN KEY (WA_BUILD_NUMBER) REFERENCES WA_BUILD_HISTORY (WA_BUILD_NUMBER),
                                                  WA_TEST_SUITE_ID INT NOT NULL AUTO_INCREMENT,
                                                  PRIMARY KEY(WA_TEST_SUITE_ID),
                                                  WA_SUITE_NAME varchar(250),
                                                  WA_TEST_DURATION BIGINT,
                                                  WA_START_TIME varchar(250),
                                                  WA_END_TIME varchar(250));


CREATE TABLE IF NOT EXISTS WA_TEST_CLASS_STAT(WA_BUILD_NUMBER integer,
                                              FOREIGN KEY (WA_BUILD_NUMBER) REFERENCES WA_BUILD_HISTORY (WA_BUILD_NUMBER),
                                              WA_TEST_SUITE_ID integer,
                                              FOREIGN KEY (WA_TEST_SUITE_ID) REFERENCES WA_TEST_SUITE_DETAIL (WA_TEST_SUITE_ID),
                                              WA_TEST_CLASS_ID INT NOT NULL AUTO_INCREMENT,PRIMARY KEY(WA_TEST_CLASS_ID),
                                              WA_TEST_CLASS_NAME varchar(250),
                                              WA_TEST_DURATION BIGINT,
                                              WA_START_TIME varchar(250),
                                              WA_END_TIME varchar(250));


CREATE TABLE IF NOT EXISTS WA_TESTCASE_STAT(WA_BUILD_NUMBER integer,
                                            FOREIGN KEY (WA_BUILD_NUMBER) REFERENCES WA_BUILD_HISTORY (WA_BUILD_NUMBER),
                                            WA_TEST_SUITE_ID integer,
                                            FOREIGN KEY (WA_TEST_SUITE_ID) REFERENCES WA_TEST_SUITE_DETAIL (WA_TEST_SUITE_ID),WA_TEST_CLASS_ID integer,
                                            FOREIGN KEY (WA_TEST_CLASS_ID) REFERENCES WA_TEST_CLASS_STAT (WA_TEST_CLASS_ID),
                                            WA_TESTCASE_ID INT NOT NULL AUTO_INCREMENT,
                                            PRIMARY KEY(WA_TESTCASE_ID),WA_TESTCASE_STATUS varchar(250),
                                            WA_TESTCASE_SIGNATURE varchar(250),WA_TESTCASE_NAME varchar(250),
                                            WA_TEST_DURATION BIGINT,WA_START_TIME varchar(250),
                                            WA_END_TIME varchar(250),WA_IS_CONFIG varchar(250));


CREATE TABLE IF NOT EXISTS WA_ERROR_DETAIL(WA_BUILD_NUMBER integer,
                                             FOREIGN KEY (WA_BUILD_NUMBER) REFERENCES WA_BUILD_HISTORY (WA_BUILD_NUMBER),
                                             WA_TESTCASE_ID integer,
                                             FOREIGN KEY (WA_TESTCASE_ID) REFERENCES WA_TESTCASE_STAT(WA_TESTCASE_ID),
                                             WA_EXCEPTION_ID INT NOT NULL AUTO_INCREMENT,
                                             PRIMARY KEY(WA_EXCEPTION_ID),
                                             WA_ERROR_TYPE varchar(250),
                                             WA_ERROR_MESSAGE text(65000),
                                             WA_ERROR_DESCRIPTION text(65000));