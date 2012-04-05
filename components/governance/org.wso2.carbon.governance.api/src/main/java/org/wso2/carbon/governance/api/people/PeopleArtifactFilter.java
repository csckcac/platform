package org.wso2.carbon.governance.api.people;

import org.wso2.carbon.governance.api.people.dataobjects.PeopleArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;

/**
 * This interface represents a mechanism to filter that can be used to identify one or more
 * People artifacts from a given set of people artifacts.
 */
public interface PeopleArtifactFilter {

    /**
     * Whether the given peopleArtifact artifact matches the expected filter criteria.
     *
     * @param peopleArtifact the peopleArtifact artifact.
     *
     * @return true if a match was found or false otherwise.
     * @throws org.wso2.carbon.governance.api.exception.GovernanceException if the operation failed.
     */
    public boolean matches(PeopleArtifact peopleArtifact) throws GovernanceException;
}
