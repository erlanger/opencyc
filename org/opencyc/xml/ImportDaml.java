package org.opencyc.xml;

import java.io.*;
import java.util.*;
import com.hp.hpl.jena.daml.*;
import com.hp.hpl.jena.daml.common.DAMLModelImpl;
import com.hp.hpl.mesa.rdf.jena.model.*;
import org.opencyc.util.*;

/**
 * Imports DAML xml content.<p>
 * <p>
 * The Another RDF Parser (ARP) is used to parse the input DAML document.
 * This class implements statement callbacks from ARP. Each triple in the
 * input file causes a call on one of the statement methods.
 * The same triple may occur more than once in a file, causing repeat calls
 * to the method.
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
public class ImportDaml {

    /**
     * The default verbosity of this application.  0 --> quiet ... 9 -> maximum
     * diagnostic input.
     */
    public static final int DEFAULT_VERBOSITY = 3;

    /**
     * Sets verbosity of this application.  0 --> quiet ... 9 -> maximum
     * diagnostic input.
     */
    protected int verbosity = DEFAULT_VERBOSITY;

    /**
     * The list of DAML web documents to import.
     */
    public static ArrayList documentsToImport = new ArrayList();

    /**
     * Ontology library nicknames, which become namespace identifiers
     * upon import into Cyc.
     */
    protected HashMap ontologyNicknames = new HashMap();

    /**
     * Constructs a new ImportDaml object.
     */
    public ImportDaml() {
    }

    /**
     * Provides the main method for the ImportDaml application.
     *
     * @param args ignored.
     */
    public static void main(String[] args) {
        Log.makeLog();
        initializeDocumentsToImport();
        //for (int i = 0; i < documentsToImport.size(); i++) {
        for (int i = 0; i < 2; i++) {
            String damlPath = (String) documentsToImport.get(i);
            ImportDaml importDaml = new ImportDaml();
            importDaml.initialize();
            importDaml.importDaml(damlPath);
        }
    }

    /**
     * Initializes the documents to import.
     */
    protected static void initializeDocumentsToImport () {
        documentsToImport.add("http://www.daml.org/experiment/ontology/agency-ont.daml");
        documentsToImport.add("http://www.daml.org/experiment/ontology/CINC-ont.daml");
        documentsToImport.add("http://www.daml.org/experiment/ontology/af-a.daml");
        documentsToImport.add("http://www.daml.org/experiment/ontology/assessment-ont.daml");
        documentsToImport.add("http://www.daml.org/experiment/ontology/economic-elements-ont.daml");
        documentsToImport.add("http://www.daml.org/experiment/ontology/elements-ont.daml");
        documentsToImport.add("http://www.daml.org/experiment/ontology/information-elements-ont.daml");
        documentsToImport.add("http://www.daml.org/experiment/ontology/infrastructure-elements-ont.daml");
        documentsToImport.add("http://www.daml.org/experiment/ontology/location-ont.daml");
        documentsToImport.add("http://www.daml.org/experiment/ontology/military-elements-ont.daml");
        documentsToImport.add("http://www.daml.org/experiment/ontology/objectives-ont.daml");
        documentsToImport.add("http://www.daml.org/experiment/ontology/operation-ont.daml");
        documentsToImport.add("http://www.daml.org/experiment/ontology/political-elements-ont.daml");
        documentsToImport.add("http://www.daml.org/experiment/ontology/social-elements-ont.daml");
        documentsToImport.add("http://www.daml.org/experiment/ontology/example1.daml");
        documentsToImport.add("http://www.daml.org/experiment/ontology/example2.daml");
    }

    /**
     * Initializes the ImportDaml object.
     */
    protected void initialize () {
        initializeOntologyNicknames();
    }

    /**
     * Initializes the ImportDaml object.
     */
    protected void initializeOntologyNicknames () {
        ontologyNicknames.put("http://www.w3.org/1999/02/22-rdf-syntax-ns", "rdf");
        ontologyNicknames.put("http://www.w3.org/2000/01/rdf-schema", "rdfs");
        ontologyNicknames.put("http://www.w3.org/2000/10/XMLSchema", "xsd");
        ontologyNicknames.put("http://www.daml.org/experiment/ontology/elements-ont", "NS0");
        ontologyNicknames.put("http://www.daml.org/2001/09/countries/fips", "fips");
        ontologyNicknames.put("http://www.daml.org/2001/10/html/airport-ont", "airport");
        ontologyNicknames.put("http://orlando.drc.com/daml/ontology/VES/3.2/drc-ves-ont", "ves");
        ontologyNicknames.put("http://www.daml.org/experiment/ontology/elements-ont", "enp");
        ontologyNicknames.put("http://www.daml.org/experiment/ontology/objectives-ont", "obj");
        ontologyNicknames.put("http://www.daml.org/experiment/ontology/social-elements-ont", "soci");
        ontologyNicknames.put("http://www.daml.org/experiment/ontology/political-elements-ont", "poli");
        ontologyNicknames.put("http://www.daml.org/experiment/ontology/economic-elements-ont", "econ");
        ontologyNicknames.put("http://www.daml.org/experiment/ontology/infrastructure-elements-ont", "infr");
        ontologyNicknames.put("http://www.daml.org/experiment/ontology/information-elements-ont", "info");
        ontologyNicknames.put("http://www.daml.org/experiment/ontology/military-elements-ont", "mil");
        ontologyNicknames.put("http://www.daml.org/experiment/ontology/ona.xsd", "dt");
        ontologyNicknames.put("http://www.daml.org/experiment/ontology/location-ont", "loc");
        ontologyNicknames.put("http://www.daml.org/experiment/ontology/assessment-ont", "assess");
        ontologyNicknames.put("http://www.daml.org/2001/02/geofile/geofile-dt.xsd", "geodt");
        ontologyNicknames.put("http://www.daml.org/experiment/ontology/cinc-ont", "cinc");
        ontologyNicknames.put("http://www.daml.org/experiment/ontology/agency-ont", "agent");
    }

    /**
     * Imports the DAML document.
     */
    protected void importDaml (String damlPath) {
        DAMLModel damlModel = new DAMLModelImpl();
        if (verbosity > 0)
            System.out.println("\nImporting " + damlPath);
        try {
            damlModel.read(damlPath);
        }
        catch (RDFException e) {
            e.printStackTrace();
        }
        System.out.println(damlModel.toString());
        System.out.println("Properties");

        Iterator iter = damlModel.listDAMLProperties();
        while (iter.hasNext()) {
            DAMLProperty c = (DAMLProperty)iter.next();
            System.out.println(c.toString());
        }


        System.out.println("Classes\n");
        iter = damlModel.listDAMLClasses();
        while (iter.hasNext()) {
            DAMLClass damlClass = (DAMLClass)iter.next();
            System.out.println(damlClass.toString());
            Literal literal = damlClass.prop_label().getValue();
            if (literal != null)
                System.out.println("  label " + literal.toString());
            LiteralAccessor comment = damlClass.prop_comment();
            if (literal != null)
                System.out.println("  comment " + comment.toString());
            Iterator iterSuperClasses = damlClass.getSuperClasses(false);
            while (iterSuperClasses.hasNext()) {
                System.out.println("  super class " + iterSuperClasses.next().toString());
            }
            Iterator iterInstances = damlClass.getInstances();
            while (iterInstances.hasNext()) {
                System.out.println("  instances " + iterInstances.next().toString());
            }

        }
        if (verbosity > 0)
            System.out.println("\nDone importing " + damlPath + "\n");
    }

    /**
     * Sets verbosity of the constraint solver output.  0 --> quiet ... 9 -> maximum
     * diagnostic input.
     *
     * @param verbosity 0 --> quiet ... 9 -> maximum diagnostic input
     */
    public void setVerbosity(int verbosity) {
        this.verbosity = verbosity;
    }



}