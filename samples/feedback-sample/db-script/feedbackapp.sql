CREATE TABLE WC_USER(
            WC_ID INTEGER NOT NULL AUTO_INCREMENT,
            WC_USER_NAME VARCHAR(255) NOT NULL,
            PRIMARY KEY (WC_ID)
)ENGINE INNODB;

CREATE TABLE WC_SESSION (
            WC_ID INTEGER NOT NULL AUTO_INCREMENT,
            WC_DATE DATE NOT NULL,
            WC_DURATION VARCHAR(255),
            WC_TEXT VARCHAR(1024),
            PRIMARY KEY (WC_ID)
)ENGINE INNODB;

CREATE TABLE WC_FEEDBACK(
            WC_ID INTEGER NOT NULL AUTO_INCREMENT,
            WC_Q_ID INT NOT NULL ,    
            WC_USER_ID INT NOT NULL,
            WC_SESSION_ID INT NOT NULL,
            WC_RATING INT,
            WC_COMMENT VARCHAR(1024),
            FOREIGN KEY(WC_SESSION_ID) REFERENCES WC_SESSION(WC_ID),
            FOREIGN KEY(WC_USER_ID) REFERENCES WC_USER(WC_ID),
            UNIQUE (WC_USER_ID, WC_SESSION_ID, WC_Q_ID),
            PRIMARY KEY (WC_ID)
)ENGINE INNODB;

CREATE TABLE WC_QUESTION(
            WC_ID INTEGER NOT NULL AUTO_INCREMENT,
            WC_Q_ID INT NOT NULL ,    
            WC_SESSION_ID INT NOT NULL,
            WC_TEXT VARCHAR(1024),
            WC_TYPE INT NOT NULL,
            FOREIGN KEY(WC_SESSION_ID) REFERENCES WC_SESSION(WC_ID),
            UNIQUE (WC_Q_ID, WC_SESSION_ID),
            PRIMARY KEY (WC_ID)
)ENGINE INNODB;

insert into WC_SESSION(WC_DATE, WC_DURATION, WC_TEXT) values 
('2011-09-12', 'total', "General"),
('2011-09-13', '09:30', "WSO2: Disrupting the middleware industry - Dr Sanjiva Weerawarana(WSO2)"),
('2011-09-13', '10:00', "Keynote: IBM Global Technology Outlook  -  2011 - Dr. C Mohan(IBM Research)"),
('2011-09-13', '11:30 : Track 01', "Using WSO2 ESB with SAP ERP (Retail) - Harsha Senanayake(John Keells Holdings)"),
('2011-09-13', '11:30 : Track 02', "Focusing your effort on strategy and vision, not infrastructure - Philippe Camus(Atlantic Lottery Corporation)"),
('2011-09-13', '12:15 : Track 01', "Delivering the Goods: Automated Quote to Cash - Brad Svee(IT Development & Engineering)"),
('2011-09-13', '12:15 : Track 02', "Open Source Adoption in the Enterprise - Prajod Vettiyattil(Wipro Technologies)"),
('2011-09-13', '14:00 : Track 01', "ESB: The Swiss Army Knife of SOA - Hiranya Jayathilake(WSO2)"),
('2011-09-13', '14:00 : Track 02', "BSS Platform - as - a - Service for Communications: Utilizing WSO2 Middleware - Ken Anderson(Datatel Solutions)"),
('2011-09-13', '14:45 : Track 01', "Data in your SOA: From SQL to NoSQL and Beyond - Sumedha Rubasighe(WSO2)"),
('2011-09-13', '14:45 : Track 02', "WSO2 at Connected Car - Andreas Wichmann(T - Systems)"),
('2011-09-13', '16:00 : Track 01', "Open Service Federation Framework - Dipanjan Sengupta(Cognizant Technology Solutions)"),
('2011-09-13', '16:00 : Track 02', "Users: The SOA Last Mile - Nuwan Bandara(WSO2)"),
('2011-09-13', '16:45', "Panel: Cloud and SOA: The good, the bad and the ugly - Paul Fremantle(WSO2)"),
('2011-09-14', '09:00' , "Keynote:Service Orientation  -  Why is it good for your business - Sastry Malladi(eBay)"),
('2011-09-14', '10:00', "Fireside chat with Sastry Malladi - Rebecca Hurst"),
('2011-09-14', '11:00 : Track 01', "Open Source Middleware for the Cloud: WSO2 Stratos - Afkham Azeez(WSO2)"),
('2011-09-14', '11:00 : Track 02', "Electronic claim flow platform for a government orchestrated process - Guillaume Devianne(Independent Consultant)"),
('2011-09-14', '11:45 : Track 01', "Building Cool Applications with WSO2 StratosLive - Selvaratnam Uthaiyashankar(WSO2)"),
('2011-09-14', '11:45 : Track 02', "SOA for Citizen Centric Government Service Delivery of Sri Lanka - Sanjaya Karunasena(ICTA)"),
('2011-09-14', '13:30 : Track 01', "SOA Governance with WSO2 Products - Senaka Fernando(WSO2)"),
('2011-09-14', '13:30 : Track 02', "Open Source Adoption in a Mexican Bank - Nelson Raimond(SHF)"),
('2011-09-14', '14:15 : Track 01', "Business Activity Monitoring in your SOA Environment - Tharindu Mathew(WSO2)"),
('2011-09-14', '14:15 : Track 02', "WSO2 in Action in Alfa Bank - Dmitry Lukyanov(Alfa - Bank Ukraine)"),
('2011-09-14', '15:30 : Track 01', "Building a Mobile POS Solution with WSO2 Carbon and Apple iPod Touch - Thilanka Kiriporuwa(Odel)"),
('2011-09-14', '15:30 : Track 02', "Security in practice - Prabath Siriwardena(WSO2)"),
('2011-09-14', '16:45', "Panel: Data, data everywhere: big, small, private, shared, public and more - Dr. Srinath Perera"),
('2011-09-15', '09:00', "Keynote: Enterprise Integration Patterns: Past, Present and Future - Gregor Hohpe(Google)"),
('2011-09-15', '09:45', "Keynote:SOA & Beyond: Using Open Source Technologies - Narendra Nathmal(Advanced SOA Center of Excellence Cognizant Technologies)"),
('2011-09-15', '11:00 : Track 01', "Using WSO2 Products in e - Government Infrastructures - Maria Belkina(Saint - Petersburg Information and Analytics Center)"),
('2011-09-15', '11:00 : Track 02', "Develop, Test and Deploy your SOA Application through a Single Platform - Chathuri Wimalasena(WSO2)"),
('2011-09-15', '11:45 : Track 01', "Multi - tenancy and Cloud Computing for eGoverment Services - Mifan Careem(Respere)"),
('2011-09-15', '11:45 : Track 02', "Quality  -  The key to successful SOA - Charitha Kankanamge(WSO2)"),
('2011-09-15', '13:30 : Track 01', "Cuban Experiences with SOA and the WSO2 Suite - Jorge Infante Osorio(Universidad de las Ciencias Informáticas)"),
('2011-09-15', '13:30 : Track 02', "Using WSO2 as a Mobile Services Platform - Simon Bilton(Gödel Technologies Europe)"),
('2011-09-15', '14:15 : Track 01', "Advanced Business Process Instance Monitoring in WSO2 Carbon - David Schumm(IAAS)"),
('2011-09-15', '14:15 : Track 02', "Building a Mobile POS Solution with WSO2 Carbon and Apple iPod Touch - Neeraj Satija(Two Degrees Mobile Limited)"),
('2011-09-15', '15:30', "Engineering to take over the world - Samisa Abeysinghe(WSO2)"),
('2011-09-15', '16:15', "Keynote: WSO2 Vision and Roadmap - Paul Fremantle(WSO2)");


insert into WC_QUESTION(WC_Q_ID, WC_SESSION_ID, WC_TEXT, WC_TYPE) values
('1', '1', "Content", '4'),
('2', '1', "Location and food", '4'),
('3', '1', "Conference experience", '4'),
('1',  '2', "WSO2: Disrupting the middleware industry -  Dr Sanjiva Weerawarana(WSO2)", '3'),
('1',  '3', "Keynote: IBM Global Technology Outlook  -  2011 - Dr. C Mohan(IBM Research)", '3'),
('1',  '4', "Using WSO2 ESB with SAP ERP (Retail) - Harsha Senanayake(John Keells Holdings)", '3'),
('1',  '5', "Focusing your effort on strategy and vision, not infrastructure - Philippe Camus(Atlantic Lottery Corporation)", '3'),
('1',  '6', "Delivering the Goods: Automated Quote to Cash - Brad Svee(IT Development & Engineering)", '3'),
('1',  '7', "Open Source Adoption in the Enterprise - Prajod Vettiyattil(Wipro Technologies)", '3'),
('1',  '8', "ESB: The Swiss Army Knife of SOA - Hiranya Jayathilake(WSO2)", '3'),
('1',  '9', "BSS Platform - as - a - Service for Communications: Utilizing WSO2 Middleware - Ken Anderson(Datatel Solutions)", '3'),
('1',  '10', "Data in your SOA: From SQL to NoSQL and Beyond - Sumedha Rubasighe(WSO2)", '3'),
('1',  '11', "WSO2 at Connected Car - Andreas Wichmann(T - Systems)", '3'),
('1',  '12', "Open Service Federation Framework - Dipanjan Sengupta(Cognizant Technology Solutions)", '3'),
('1',  '13', "Users: The SOA Last Mile - Nuwan Bandara(WSO2)", '3'),
('1',  '14', "Panel: Cloud and SOA: The good, the bad and the ugly - Paul Fremantle(WSO2)", '3'),
('1',  '15' , "Keynote:Service Orientation  -  Why is it good for your business - Sastry Malladi(eBay)", '3'),
('1',  '16', "Fireside chat with Sastry Malladi - Rebecca Hurst", '3'),
('1',  '17', "Building Cool Applications with WSO2 StratosLive - Selvaratnam Uthaiyashankar(WSO2)", '3'),
('1',  '18', "Using WSO2 Products in e - Government Infrastructures - Maria Belkina(Saint - Petersburg Information and Analytics Center)", '3'),
('1',  '19', "Open Source Middleware for the Cloud: WSO2 Stratos - Afkham Azeez(WSO2)", '3'),
('1',  '20', "SOA for Citizen Centric Government Service Delivery of Sri Lanka - Sanjaya Karunasena(ICTA)", '3'),
('1',  '21', "SOA Governance with WSO2 Products - Senaka Fernando(WSO2)", '3'),
('1',  '22', "Open Source Adoption in a Mexican Bank - Nelson Raimond(SHF)", '3'),
('1',  '23', "Business Activity Monitoring in your SOA Environment - Tharindu Mathew(WSO2)", '3'),
('1',  '24', "WSO2 in Action in Alfa Bank - Dmitry Lukyanov(WSO2)", '3'),
('1',  '25', "A Dynamic Telecommunications SOA platform  - Neeraj Satija(Two Degrees Mobile Limited)", '3'),
('1',  '26', "Security in practice - Prabath Siriwardena(WSO2)", '3'),
('1',  '27', "Panel: Data, data everywhere: big, small, private, shared, public and more - Dr. Srinath Perera", '3'),
('1',  '28', "Keynote: Enterprise Integration Patterns: Past, Present and Future - Gregor Hohpe(google)", '3'),
('1',  '29', "Keynote:SOA & Beyond: Using Open Source Technologies - Narendra Nathmal(Advanced SOA Center of Excellence Cognizant Technologies)", '3'),
('1',  '30', "Electronic claim flow platform for a government orchestrated process - Guillaume Devianne(Independent Consultant)", '3'),
('1',  '31', "Develop, Test and Deploy your SOA Application through a Single Platform - Chathuri Wimalasena(WSO2)", '3'),
('1',  '32', "Multi - tenancy and Cloud Computing for eGoverment Services - Mifan Careem(Respere)", '3'),
('1',  '33', "Quality  -  The key to successful SOA - Charitha Kankanamge(WSO2)", '3'),
('1',  '34', "Cuban Experiences with SOA and the WSO2 Suite - Jorge Infante Osorio(Universidad de las Ciencias Informáticas)", '3'),
('1',  '35', "Using WSO2 as a Mobile Services Platform - Simon Bilton(Gödel Technologies Europe)", '3'),
('1',  '36', "Advanced Business Process Instance Monitoring in WSO2 Carbon - David Schumm(IAAS)", '3'),
('1',  '37', "Building a Mobile POS Solution with WSO2 Carbon and Apple iPod Touch - Thilanka Kiriporuwa(Odel)", '3'),
('1',  '38', "Engineering to take over the world - Samisa Abeysinghe(WSO2)", '3'),
('1',  '39', "Keynote: WSO2 Vision and Roadmap - Paul Fremantle(WSO2)", '3');


