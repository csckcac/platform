#!/bin/bash
nova boot --key_name=nova-key --flavor=1 --image=6efe0a7a-2f01-40b2-90a2-6de86c7ceaa6 wso2-jetty-domain-21-1693 --user_data=/root/client/payload.zip
