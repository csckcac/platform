package org.wso2.carbon.messagebox.admin.internal;

import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.messagebox.MessageBoxConstants;
import org.wso2.carbon.messagebox.MessageBoxException;
import org.wso2.carbon.messagebox.admin.internal.exception.MessageBoxAdminException;
import org.wso2.carbon.messagebox.admin.internal.util.MessageBoxHolder;
import org.wso2.carbon.messagebox.queue.QueueManager;
import org.wso2.carbon.messagebox.queue.QueueRolePermission;
import org.wso2.carbon.messagebox.queue.QueueUserPermission;

import java.util.ArrayList;
import java.util.List;


public class QueueManagerAdminService extends AbstractAdmin {

    public Queue[] getAllQueues(int startingIndex, int maxQueueCount)
            throws MessageBoxAdminException {
        List<Queue> allQueues = new ArrayList<Queue>();
        try {
            QueueManager queueManager =
                    MessageBoxHolder.getInstance().getMessageboxService().getQueueManager();
            List<org.wso2.carbon.messagebox.queue.Queue> queues = queueManager.getAllQueues();
            int index = 0;
            int queueIndex = 0;
            for (org.wso2.carbon.messagebox.queue.Queue queue : queues) {
                if (startingIndex == index || startingIndex < index) {
                    Queue queueDTO = new Queue(queue.getName());
                    queueDTO.setCreatedFrom(queue.getCreatedFrom());
                    queueDTO.setCreatedTime(queue.getCreatedTime());
                    queueDTO.setUpdatedTime(queue.getUpdatedTime());
                    queueDTO.setQueueDepth(queue.getQueueDepth());
                    queueDTO.setMessageCount(queue.getMessageCount());
                    allQueues.add(queueDTO);
                    queueIndex++;
                    if (queueIndex == maxQueueCount) {
                        break;
                    }
                }
                index++;
            }
            return allQueues.toArray(new Queue[allQueues.size()]);
        } catch (MessageBoxException e) {
            throw new MessageBoxAdminException("Can not get the queue manager ", e);
        }
    }

    public int getQueuesCount() throws MessageBoxAdminException {
        try {
            QueueManager queueManager = MessageBoxHolder.getInstance().getMessageboxService().getQueueManager();
            return queueManager.getAllQueues().size();
        } catch (MessageBoxException e) {
            throw new MessageBoxAdminException("Failed to get total number of queues.", e);
        }
    }

    public QueueUserPermissionBean[] getQueueUserPermissions(String qName)
            throws MessageBoxAdminException {
        List<QueueUserPermissionBean> adminQueueUserPermissions = new ArrayList<QueueUserPermissionBean>();
        try {
            QueueManager queueManager =
                    MessageBoxHolder.getInstance().getMessageboxService().getQueueManager();
            for (QueueUserPermission queueUserPermission :
                    queueManager.getQueueUserPermissions(qName)) {
                QueueUserPermissionBean adminQueueUserPermission = new QueueUserPermissionBean();
                adminQueueUserPermission.setUserName(queueUserPermission.getUserName());
                adminQueueUserPermission.setAllowedToConsume(queueUserPermission.isAllowedToConsume());
                adminQueueUserPermission.setAllowedToPublish(queueUserPermission.isAllowedToPublish());
                adminQueueUserPermissions.add(adminQueueUserPermission);
            }
            return adminQueueUserPermissions.toArray(new QueueUserPermissionBean[adminQueueUserPermissions.size()]);
        } catch (MessageBoxException e) {
            throw new MessageBoxAdminException("Unable to access the queue manager", e);
        }

    }

    public QueueRolePermissionBean[] getQueueRolePermissions(
            String qName)
            throws MessageBoxAdminException {
        List<QueueRolePermissionBean> adminQueueRolePermissions = new ArrayList<QueueRolePermissionBean>();
        try {
            QueueManager queueManager =
                    MessageBoxHolder.getInstance().getMessageboxService().getQueueManager();
            for (QueueRolePermission queueRolePermission :
                    queueManager.getQueueRolePermissions(qName)) {
                QueueRolePermissionBean adminQueueRolePermission = new QueueRolePermissionBean();
                adminQueueRolePermission.setRoleName(queueRolePermission.getRoleName());
                adminQueueRolePermission.setAllowedToConsume(queueRolePermission.isAllowedToConsume());
                adminQueueRolePermission.setAllowedToPublish(queueRolePermission.isAllowedToPublish());
                adminQueueRolePermissions.add(adminQueueRolePermission);
            }
            return adminQueueRolePermissions.toArray(new QueueRolePermissionBean[adminQueueRolePermissions.size()]);
        } catch (MessageBoxException e) {
            throw new MessageBoxAdminException("Unable to access the queue manager", e);
        }

    }

    public void addQueue(String queueName) throws MessageBoxAdminException {
        try {
            QueueManager queueManager =
                    MessageBoxHolder.getInstance().getMessageboxService().getQueueManager();
            if(queueManager.isQueueExists(queueName)){
                throw new MessageBoxAdminException("Queue already exists with name : "+ queueName);
            }else{
                queueManager.addQueue(queueName, MessageBoxConstants.MB_QUEUE_CREATED_FROM_AMQP);
            }
        } catch (MessageBoxException e) {
            throw new MessageBoxAdminException("Failed to add the queue:"+queueName, e);
        }
    }

    public void updateUserPermissions(String queueName,
                                      QueueUserPermissionBean[] adminQueueUserPermissions)
            throws MessageBoxAdminException {
        List<org.wso2.carbon.messagebox.queue.QueueUserPermission> queueUserPermissions
                = new ArrayList<org.wso2.carbon.messagebox.queue.QueueUserPermission>();
        try {
            QueueManager queueManager =
                    MessageBoxHolder.getInstance().getMessageboxService().getQueueManager();
            for (QueueUserPermissionBean adminQueueUserPermission : adminQueueUserPermissions) {
                QueueUserPermission queueUserPermission = new QueueUserPermission();
                queueUserPermission.setUserName(adminQueueUserPermission.getUserName());
                queueUserPermission.setAllowedToConsume(adminQueueUserPermission.isAllowedToConsume());
                queueUserPermission.setAllowedToPublish(adminQueueUserPermission.isAllowedToPublish());
                queueUserPermissions.add(queueUserPermission);
            }
            queueManager.updateUserPermission(queueUserPermissions, queueName);
        } catch (MessageBoxException e) {
            throw new MessageBoxAdminException("Unable to access the queue manager", e);
        }
    }

    public void updateRolePermissions(String queueName,
                                      QueueRolePermissionBean[] adminQueueRolePermissions)
            throws MessageBoxAdminException {
        List<org.wso2.carbon.messagebox.queue.QueueRolePermission> queueRolePermissions
                = new ArrayList<QueueRolePermission>();
        try {
            QueueManager queueManager =
                    MessageBoxHolder.getInstance().getMessageboxService().getQueueManager();
            for (QueueRolePermissionBean adminQueueRolePermission : adminQueueRolePermissions) {
                QueueRolePermission queueRolePermission = new QueueRolePermission();
                queueRolePermission.setRoleName(adminQueueRolePermission.getRoleName());
                queueRolePermission.setAllowedToConsume(adminQueueRolePermission.isAllowedToConsume());
                queueRolePermission.setAllowedToPublish(adminQueueRolePermission.isAllowedToPublish());
                queueRolePermissions.add(queueRolePermission);
            }
            queueManager.updateRolePermission(queueRolePermissions, queueName);
        } catch (MessageBoxException e) {
            throw new MessageBoxAdminException("Unable to access the queue manager", e);
        }
    }

    public void deleteQueue(String queueName) throws MessageBoxAdminException {
        try {
            QueueManager queueManager =
                    MessageBoxHolder.getInstance().getMessageboxService().getQueueManager();
            queueManager.deleteQueue(queueName);
        } catch (MessageBoxException e) {
            throw new MessageBoxAdminException(e.getMessage());
        }
    }

}
