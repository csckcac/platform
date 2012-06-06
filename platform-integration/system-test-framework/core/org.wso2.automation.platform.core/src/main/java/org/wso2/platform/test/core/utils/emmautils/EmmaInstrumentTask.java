/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.platform.test.core.utils.emmautils;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EmmaInstrumentTask extends AbstractEmmaMojo{
    public void emmaInstrument()
    {

    }



    public List pluginClasspath;

    /**
     * Indicates whether the metadata should be merged into the destination <code>metadataFile</code>, if any.
     *
     * @parameter expression="${emma.merge}" default-value="true"
     */
    public boolean merge;

    /**
     * Instrumentation filters.
     *
     * @parameter
     */
    public String[] filters;

    /**
     * Specifies the instrumentation paths to use.
     *
     * @parameter
     */
    public File[] instrumentationPaths;

    /**
     * Location to store class coverage metadata.
     *
     * @parameter expression="${emma.metadataFile}" default-value="${project.build.directory}/coverage.em"
     */
    public File metadataFile;

    /**
     * Artifact factory.
     *
     * @component
     */
    public ArtifactFactory factory;

    /**
     * Checks the parameters before doing the work.
     *
     * @throws MojoExecutionException if things go wrong.
     * @throws MojoFailureException if things go wrong.
     */
    protected void checkParameters()
            throws MojoExecutionException, MojoFailureException
    {
        super.checkParameters();

        if ( filters == null )
        {
            filters = new String[0];
        }

        if ( instrumentationPaths == null )
        {
            instrumentationPaths = new File[] { new File("/home/dharshana/automation/coverage3/system-test-framework/core/org.wso2.automation.platform.core/src/main/resources/emma/home/"  ) };
        }
    }

    protected void doExecute()
            throws MojoExecutionException, MojoFailureException
    {
        if ( getLog().isDebugEnabled() )
        {
            if ( instrumentationPaths != null )
            {
                getLog().debug( "Instrumentation path:" );
                for ( int i = 0; i < instrumentationPaths.length; ++i )
                {
                    getLog().debug( " o " + instrumentationPaths[i].getAbsolutePath() );
                }
            }

            if ( filters != null && filters.length > 0 )
            {
                getLog().debug( "Filters:" );
                for ( int i = 0; i < filters.length; ++i )
                {
                    getLog().debug( " o " + filters[i] );
                }
            }
        }

        final InstrumentTask task = new InstrumentTask();
        task.setOutputDirectory( outputDirectory );
        task.setFilters( filters );
        task.setVerbose( verbose );
        task.setInstrumentationPaths( instrumentationPaths );
        task.setMerge( merge );
        task.setMetadataFile( metadataFile );

        getLog().info( "Instrumenting classes with EMMA" );
        try
        {
            task.execute();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }

        // prepare test execution by adding EMMA dependencies
        addEmmaDependenciesToTestClasspath();

        final File classesDir = new File( outputDirectory, "classes" );
        project.getBuild().setOutputDirectory( classesDir.getPath() );
    }


    private void addEmmaDependenciesToTestClasspath()
            throws MojoExecutionException
    {
        // look for EMMA dependency in this plugin classpath
        final Map pluginArtifactMap = ArtifactUtils.artifactMapByVersionlessId(pluginClasspath);
        Artifact emmaArtifact = (Artifact) pluginArtifactMap.get( "emma:emma" );

        if ( emmaArtifact == null )
        {
            // this should not happen
            throw new MojoExecutionException( "Failed to find 'emma' artifact in plugin dependencies" );
        }

        // set EMMA dependency scope to test
        emmaArtifact = artifactScopeToTest( emmaArtifact );

        // add EMMA to project dependencies
        final Set deps = new HashSet();
        if ( project.getDependencyArtifacts() != null )
        {
            deps.addAll( project.getDependencyArtifacts() );
        }
        deps.add( emmaArtifact );
        project.setDependencyArtifacts( deps );
    }

    /**
     * Convert an artifact to a test artifact.
     *
     * @param artifact to convert
     * @return an artifact with a test scope
     */
    private Artifact artifactScopeToTest( Artifact artifact )
    {
        return factory.createArtifact( artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(),
                                       Artifact.SCOPE_TEST, artifact.getType() );
    }
}
