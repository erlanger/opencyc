package org.opencyc.xml;

import java.io.*;
import java.net.*;
import java.util.*;
import org.opencyc.api.*;
import org.opencyc.util.*;

/**
 * Imports DAML xml content for the DAML SONAT ontologies.<p>
 *
 * @version $Id$
 * @author Stephen L. Reed
 *
 * <p>Copyright 2001 Cycorp, Inc., license is open source GNU LGPL.
 * <p><a href="http://www.opencyc.org/license.txt">the license</a>
 * <p><a href="http://www.opencyc.org">www.opencyc.org</a>
 * <p><a href="http://www.sourceforge.net/projects/opencyc">OpenCyc at SourceForge</a>
 * <p>
 * THIS SOFTWARE AND KNOWLEDGE BASE CONTENT ARE PROVIDED ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE OPENCYC
 * ORGANIZATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE AND KNOWLEDGE
 * BASE CONTENT, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

public class ImportSonatDaml {

    /**
     * The list of DAML documents and import microtheories.
     */
    protected ArrayList damlDocInfos = new ArrayList();

    /**
     * CycAccess object to manage api connection the the Cyc server.
     */
    protected CycAccess cycAccess;

    /**
     * Ontology library nicknames, which become namespace identifiers
     * upon import into Cyc.
     * namespace uri --> ontologyNickname
     */
    protected HashMap ontologyNicknames = new HashMap();

    /**
     * The name of the KB Subset collection which identifies ontology import
     * terms in Cyc.
     */
    protected String kbSubsetCollectionName = "DamlSonatConstant";

    /**
     * Constructs a new ImportSonatDaml object.
     */
    public ImportSonatDaml() {
    }

    /**
     * Provides the main method for the ImportSonatDaml application.
     *
     * @param args ignored.
     */
    public static void main(String[] args) {
        Log.makeLog();
        ImportSonatDaml importSonatDaml = new ImportSonatDaml();
        try {
            importSonatDaml.importDaml();
        }
        catch (Exception e) {
            Log.current.printStackTrace(e);
            System.exit(1);
        }
    }

    /**
     * Import the SONAT DAML ontologies into Cyc.
     */
    protected void importDaml ()
        throws IOException, UnknownHostException, CycApiException {

        initializeDocumentsToImport();
        initializeOntologyNicknames();
        ImportDaml importDaml =
            new ImportDaml(cycAccess,
                           ontologyNicknames,
                           kbSubsetCollectionName);
        for (int i = 16; i < 17; i++) {
        //for (int i = 0; i < documentsToImport.size(); i++) {
        //for (int i = 0; i < 5; i++) {
            DamlDocInfo damlDocInfo = (DamlDocInfo) damlDocInfos.get(i);
            String damlPath = damlDocInfo.getDamlPath();
            String importMt = damlDocInfo.getImportMt();
            importDaml.initialize();
            importDaml.importDaml(damlPath, importMt);
        }
    }

    /**
     * Initializes the documents to import.
     */
    protected void initializeDocumentsToImport () {
        damlDocInfos.add(new DamlDocInfo("http://orlando.drc.com/daml/ontology/VES/3.2/drc-ves-ont.daml",
                                         "DamlSonatDrcVesOntologyMt"));
        damlDocInfos.add(new DamlDocInfo("http://www.daml.org/2001/10/html/airport-ont.daml",
                                         "DamlSonatAirportOntologyMt"));
        damlDocInfos.add(new DamlDocInfo("http://www.daml.org/2001/09/countries/fips.daml",
                                         "DamlSonatFipsOntologyMt"));
        damlDocInfos.add(new DamlDocInfo("http://www.daml.org/2001/09/countries/fips-10-4.daml",
                                         "DamlSonatFips10-4OntologyMt"));
        damlDocInfos.add(new DamlDocInfo("http://www.daml.org/2001/12/factbook/factbook-ont.daml",
                                         "DamlSonatCiaFactbookOntologyMt"));
        damlDocInfos.add(new DamlDocInfo("http://www.daml.org/experiment/ontology/agency-ont.daml",
                                         "DamlSonatAgencyOntologyMt"));
        damlDocInfos.add(new DamlDocInfo("http://www.daml.org/experiment/ontology/CINC-ont.daml",
                                         "DamlSonatCincOntologyMt"));
        damlDocInfos.add(new DamlDocInfo("http://www.daml.org/experiment/ontology/af-a.daml",
                                         "DamlSonatAfghanistanAOntologyMt"));
        damlDocInfos.add(new DamlDocInfo("http://www.daml.org/experiment/ontology/assessment-ont.daml",
                                         "DamlSonatAssessmentOntologyMt"));
        damlDocInfos.add(new DamlDocInfo("http://www.daml.org/experiment/ontology/economic-elements-ont.daml",
                                         "DamlSonatEconomicElementsOntologyMt"));
        damlDocInfos.add(new DamlDocInfo("http://www.daml.org/experiment/ontology/elements-ont.daml",
                                         "DamlSonatElementsOfNationalPowerOntologyMt"));
        damlDocInfos.add(new DamlDocInfo("http://www.daml.org/experiment/ontology/information-elements-ont.daml",
                                         "DamlSonatInformationElementsOntologyMt"));
        damlDocInfos.add(new DamlDocInfo("http://www.daml.org/experiment/ontology/infrastructure-elements-ont.daml",
                                         "DamlSonatInfrastructureElementsOntologyMt"));
        damlDocInfos.add(new DamlDocInfo("http://www.daml.org/experiment/ontology/location-ont.daml",
                                         "DamlSonatLocationOntologyMt"));
        damlDocInfos.add(new DamlDocInfo("http://www.daml.org/experiment/ontology/military-elements-ont.daml",
                                         "DamlSonatMilitaryElementsOntologyMt"));
        damlDocInfos.add(new DamlDocInfo("http://www.daml.org/experiment/ontology/objectives-ont.daml",
                                         "DamlSonatObjectivesOntologyMt"));
        damlDocInfos.add(new DamlDocInfo("http://www.daml.org/experiment/ontology/operation-ont.daml",
                                         "DamlSonatOperationOntologyMt"));
        damlDocInfos.add(new DamlDocInfo("http://www.daml.org/experiment/ontology/political-elements-ont.daml",
                                         "DamlSonatPoliticalElementsOntologyMt"));
        damlDocInfos.add(new DamlDocInfo("http://www.daml.org/experiment/ontology/social-elements-ont.daml",
                                         "DamlSonatSocialElementsOntologyMt"));
        damlDocInfos.add(new DamlDocInfo("http://www.daml.org/experiment/ontology/example1.daml",
                                         "DamlSonatExample1OntologyMt"));
        damlDocInfos.add(new DamlDocInfo("http://www.daml.org/experiment/ontology/example2.daml",
                                         "DamlSonatExample2OntologyMt"));
    }

    /**
     * Initializes the Ontology nicknames mapping.
     */
    protected void initializeOntologyNicknames () {
        ontologyNicknames.put("http://www.w3.org/1999/02/22-rdf-syntax-ns", "rdf");
        ontologyNicknames.put("http://www.w3.org/2000/01/rdf-schema", "rdfs");
        ontologyNicknames.put("http://www.w3.org/2000/10/XMLSchema", "xsd");

        ontologyNicknames.put("http://www.daml.org/2001/03/daml+oil", "daml");

        ontologyNicknames.put("http://orlando.drc.com/daml/Ontology/daml-extension/3.2/daml-ext-ont", "daml-ext");

        ontologyNicknames.put("http://www.daml.org/2001/12/factbook/factbook-ont.daml", "factbook");

        ontologyNicknames.put("http://orlando.drc.com/daml/ontology/VES/3.2/drc-ves-ont.daml", "ves");
        ontologyNicknames.put("http://orlando.drc.com/daml/ontology/VES/3.2/drc-ves-ont", "ves");

        ontologyNicknames.put("http://www.daml.org/2001/10/html/airport-ont.daml", "airport");

        ontologyNicknames.put("http://www.daml.org/2001/09/countries/fips-10-4-ont", "fips10-4");

        ontologyNicknames.put("http://www.daml.org/2001/09/countries/fips.daml", "fips");

        ontologyNicknames.put("http://www.daml.org/experiment/ontology/elements-ont.daml", "enp");
        ontologyNicknames.put("http://www.daml.org/experiment/ontology/elements-ont", "enp");

        ontologyNicknames.put("http://www.daml.org/experiment/ontology/objectives-ont.daml", "obj");
        ontologyNicknames.put("http://www.daml.org/experiment/ontology/objectives-ont", "obj");

        ontologyNicknames.put("http://www.daml.org/experiment/ontology/social-elements-ont.daml", "soci");
        ontologyNicknames.put("http://www.daml.org/experiment/ontology/social-elements-ont", "soci");

        ontologyNicknames.put("http://www.daml.org/experiment/ontology/political-elements-ont.daml", "poli");
        ontologyNicknames.put("http://www.daml.org/experiment/ontology/political-elements-ont", "poli");

        ontologyNicknames.put("http://www.daml.org/experiment/ontology/economic-elements-ont.daml", "econ");
        ontologyNicknames.put("http://www.daml.org/experiment/ontology/economic-elements-ont", "econ");

        ontologyNicknames.put("http://www.daml.org/experiment/ontology/infrastructure-elements-ont.daml", "infr");
        ontologyNicknames.put("http://www.daml.org/experiment/ontology/infrastructure-elements-ont", "infr");

        ontologyNicknames.put("http://www.daml.org/experiment/ontology/information-elements-ont.daml", "info");
        ontologyNicknames.put("http://www.daml.org/experiment/ontology/information-elements-ont", "info");

        ontologyNicknames.put("http://www.daml.org/experiment/ontology/military-elements-ont.daml", "mil");
        ontologyNicknames.put("http://www.daml.org/experiment/ontology/military-elements-ont", "mil");

        ontologyNicknames.put("http://www.daml.org/experiment/ontology/ona.xsd", "dt");

        ontologyNicknames.put("http://www.daml.org/experiment/ontology/location-ont.daml", "loc");
        ontologyNicknames.put("http://www.daml.org/experiment/ontology/location-ont", "loc");

        ontologyNicknames.put("http://www.daml.org/experiment/ontology/assessment-ont.daml", "assess");
        ontologyNicknames.put("http://www.daml.org/experiment/ontology/assessment-ont", "assess");

        ontologyNicknames.put("http://www.daml.org/2001/02/geofile/geofile-dt.xsd", "geodt");

        ontologyNicknames.put("http://www.daml.org/experiment/ontology/CINC-ont.daml", "cinc");
        ontologyNicknames.put("http://www.daml.org/experiment/ontology/CINC-ont", "cinc");
        ontologyNicknames.put("http://www.daml.org/experiment/ontology/cinc-ont", "cinc");

        ontologyNicknames.put("http://www.daml.org/experiment/ontology/agency-ont.daml", "agent");
        ontologyNicknames.put("http://www.daml.org/experiment/ontology/agency-ont", "agent");

        ontologyNicknames.put("http://www.daml.org/experiment/ontology/operation-ont.daml", "oper");
        ontologyNicknames.put("http://www.daml.org/experiment/ontology/operation-ont", "oper");
    }

    /**
     * Provides a container for specifying the SONAT DAML document paths and
     * the Cyc import microtheory for each.
     */
    protected class DamlDocInfo {
        /**
         * path (url) to the SONAT DAML document
         */
        protected String damlPath;

        /**
         * microtheory into which DAML content is imported
         */
        protected String importMt;

        public DamlDocInfo (String damlPath, String importMt) {
            this.damlPath = damlPath;
            this.importMt = importMt;
        }

        /**
         * Returns the daml document path.
         *
         * @return the daml document path
         */
        public String getDamlPath () {
            return damlPath;
        }

        /**
         * Returns the microtheory into which DAML content is imported.
         *
         * @return the microtheory into which DAML content is imported
         */
        public String getImportMt () {
            return importMt;
        }
    }
}