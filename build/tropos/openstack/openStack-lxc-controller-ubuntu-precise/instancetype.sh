#!/bin/bash
nova-manage flavor create --name=m1.dam --memory=128 --cpu=1 --root_gb=2 --ephemeral_gb=0 --flavor=6 --swap=0 --rxtx_factor=1
