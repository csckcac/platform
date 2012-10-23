#!/bin/bash
nova-manage flavor create --name=m1.wso2 --memory=1024 --cpu=1 --root_gb=2 --ephemeral_gb=0 --flavor=6 --swap=0 --rxtx_factor=1
