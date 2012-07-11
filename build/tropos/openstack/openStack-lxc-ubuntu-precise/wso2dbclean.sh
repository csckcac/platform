#!/bin/bash
# ----------------------------------------------------------------------------
#  Copyright 2005-20012 WSO2, Inc. http://www.wso2.org
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

# ----------------------------------------------------------------------------
 
DEFAULT_ID=all
while getopts i:hy opts
do
  case $opts in
    i)
        ID=${OPTARG}
        ;;
    h)
        usage
        ;;
  esac
done

if [ -z ${ID} ]
then
        ID=${DEFAULT_ID}
fi

if [ $ID == "all" ]; then
	mysql -uroot -popenstack -e "use nova; delete from security_group_instance_association;"
	mysql -uroot -popenstack -e "use nova; delete from instance_info_caches;"
	mysql -uroot -popenstack -e "use nova; delete from instances;"
	mysql -uroot -popenstack -e "use nova; update fixed_ips set allocated=0;"
	mysql -uroot -popenstack -e "use nova; update fixed_ips set leased=0;"
	mysql -uroot -popenstack -e "use nova; update fixed_ips set reserved=0;"
	mysql -uroot -popenstack -e "use nova; update fixed_ips set instance_id=NULL;"
	mysql -uroot -popenstack -e "use nova; update fixed_ips set virtual_interface_id=NULL;"
	mysql -uroot -popenstack -e "use nova; update fixed_ips set updated_at=NULL;"
	mysql -uroot -popenstack -e "use nova; update fixed_ips set host=NULL;"

	mysql -uroot -popenstack -e "use nova; update floating_ips set project_id=NULL;"
	mysql -uroot -popenstack -e "use nova; update floating_ips set fixed_ip_id=NULL;"
	mysql -uroot -popenstack -e "use nova; update floating_ips set updated_at=NULL;"
	mysql -uroot -popenstack -e "use nova; update floating_ips set auto_assigned=0;"
	mysql -uroot -popenstack -e "use nova; update floating_ips set host=NULL;"
        
        mysql -uroot -popenstack -e "use nova; delete from virtual_interfaces;"
	mysql -uroot -popenstack -e "use nova; delete from instance_faults;"

fi

usage() {
cat << USAGE
Syntax
    DBclean.sh -i { instance id }
    -i: instance id to delete
USAGE
exit 1
}

