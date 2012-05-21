@echo off

REM ---------------------------------------------------------------------------
REM        Copyright 2005-2012 WSO2, Inc. http://www.wso2.org
REM
REM  Licensed under the Apache License, Version 2.0 (the "License");
REM  you may not use this file except in compliance with the License.
REM  You may obtain a copy of the License at
REM
REM      http://www.apache.org/licenses/LICENSE-2.0
REM
REM  Unless required by applicable law or agreed to in writing, software
REM  distributed under the License is distributed on an "AS IS" BASIS,
REM  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM  See the License for the specific language governing permissions and
REM  limitations under the License.

rem ---------------------------------------------------------------------------
rem  Samples Script for deploying WSO2 BPS samples
rem
rem Environment Variable Prequisites
rem
rem   CARBON_HOME   Home of CARBON installation. If not set I will  try
rem                   to figure it out.
rem
rem   JAVA_HOME       Must point at your Java Development Kit installation.
rem
rem   JAVA_OPTS       (Optional) Java runtime options used when the commands
rem                   is executed.
rem ---------------------------------------------------------------------------

SET cn=1

set CMD=%*

:initial
if "%1"=="-s" goto sname
if "%1"=="" goto no_sample
shift
goto initial

:sname
shift
set cn=%1
if "%1"=="" goto invalid_sample_name
echo .\..\repository\samples\bpel\%cn%
IF EXIST .\..\repository\samples\bpel\%cn% goto copy_samples
goto missing_sample 

:lreturn
shift
goto initial

:copy_samples
xcopy /I /E /Y .\..\repository\samples\bpel\%cn% .\..\repository\deployment\server\bpel\
goto run

:run
wso2server.bat
goto done

:missing_sample
echo "The specified sample does not exist in the file system. Please specify an existing sample"
goto done

:invalid_sample_name
echo "*** Specified sample is not present *** Please specify a valid sample with the -s option"
echo "Example, to run sample HelloWorld1.zip : wso2esb-samples.sh -s HelloWorld1.zip"
goto done

:no_sample
echo "*** Sample to be started is not specified *** Please specify a sample to be started with the -s option"
echo "Example, to run sample HelloWorld1.zip : wso2esb-samples.sh -sn HelloWorld1.zip"
goto done

:done
