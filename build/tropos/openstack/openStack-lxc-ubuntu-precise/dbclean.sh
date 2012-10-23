#!/bin/bash
 
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

