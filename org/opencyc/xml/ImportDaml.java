package org.opencyc.xml;

import java.io.*;
import java.net.*;
import java.util.*;
import org.xml.sax.*;
import com.hp.hpl.jena.rdf.arp.*;
import com.hp.hpl.mesa.rdf.jena.common.*;
import com.hp.hpl.mesa.rdf.jena.model.*;
import org.opencyc.api.*;
import org.opencyc.cycobject.*;
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
public class ImportDaml implements StatementHandler {

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
     * Another RDF Parser instance.
     */
    protected ARP arp;

    /**
     * Cyc terms which have semantic counterparts in DAML.
     * DAML term --> Cyc term
     */
    protected static HashMap equivalentDamlCycTerms;

    /**
     * Ontology library nicknames, which become namespace identifiers
     * upon import into Cyc.
     * namespace uri --> ontologyNickname
     */
    protected HashMap ontologyNicknames;

    /**
     * Previously imported term used to avoid redundant assertions.
     */
    protected DamlTermInfo previousDamlTermInfo = null;

    /**
     * Ontology import microtheory name.
     */
    protected String importMtName;

    /**
     * Ontology import microtheory.
     */
    protected CycConstant importMt;

    /**
     * The current DAML Restriction object being constructed from sequential
     * RDF triples.
     */
    protected DamlRestriction damlRestriction;

    /**
     * CycAccess object to manage api connection the the Cyc server.
     */
    protected CycAccess cycAccess;

    /**
     * The name of the KB Subset collection which identifies ontology import
     * terms in Cyc.
     */
    protected String kbSubsetCollectionName;

    /**
     * The KB Subset collection which identifies ontology import
     * terms in Cyc.
     */
    protected CycConstant kbSubsetCollection;

    /**
     * The #$BookkeepingMt.
     */
    protected CycConstant bookkeepingMt;

    /**
     * Constructs a new ImportDaml object.
     *
     * @param cycAccess the CycAccess instance which manages the connection
     * to the Cyc server and provides Cyc API services
     * @param ontologyNicknames the dictionary associating each ontology uri with
     * the nickname used for the Cyc namespace qualifier
     * @param kbSubsetCollectionName the name of the Cyc KbSubsetCollection
     * which identifies each of the imported terms
     */
    public ImportDaml(CycAccess cycAccess,
                      HashMap ontologyNicknames,
                      String kbSubsetCollectionName) {
        this.cycAccess = cycAccess;
        this.ontologyNicknames = ontologyNicknames;
        this.kbSubsetCollectionName = kbSubsetCollectionName;
        arp = new ARP();
        arp.setStatementHandler(this);
    }

    /**
     * Initializes the ImportDaml object.
     */
    protected void initialize ()
        throws IOException, UnknownHostException, CycApiException {
        if (equivalentDamlCycTerms == null) {
            equivalentDamlCycTerms = new HashMap();

            //TODO - reify this mapping and discard this dictionary.

            // Collections
            equivalentDamlCycTerms.put("daml:Thing", "Thing");
            equivalentDamlCycTerms.put("rdfs:Resource", "Thing");

            equivalentDamlCycTerms.put("daml:Class", "Collection");
            equivalentDamlCycTerms.put("rdfs:Class", "Collection");

            equivalentDamlCycTerms.put("daml:Ontology", "AbstractInformationStructure");

            equivalentDamlCycTerms.put("daml:DatatypeProperty", "DamlDatatypeProperty");

            equivalentDamlCycTerms.put("daml:ObjectProperty", "DamlObjectProperty");

            equivalentDamlCycTerms.put("daml:Property", "BinaryPredicate");
            equivalentDamlCycTerms.put("rdfs:Property", "BinaryPredicate");
            equivalentDamlCycTerms.put("rdf:Property", "BinaryPredicate");

            equivalentDamlCycTerms.put("daml:TransitiveProperty", "TransitiveBinaryPredicate");

            equivalentDamlCycTerms.put("daml:Literal", "SubLAtomicTerm");
            equivalentDamlCycTerms.put("rdfs:Literal", "SubLAtomicTerm");

            equivalentDamlCycTerms.put("xsd:string", "CharacterString");

            // Binary predicates
            equivalentDamlCycTerms.put("daml:subClassOf", "genls");
            equivalentDamlCycTerms.put("rdfs:subClassOf", "genls");

            equivalentDamlCycTerms.put("daml:type", "isa");
            equivalentDamlCycTerms.put("rdfs:type", "isa");
            equivalentDamlCycTerms.put("rdf:type", "isa");

            equivalentDamlCycTerms.put("daml:subPropertyOf", "genlPreds");
            equivalentDamlCycTerms.put("rdfs:subPropertyOf", "genlPreds");

            equivalentDamlCycTerms.put("daml:label", "nameString");
            equivalentDamlCycTerms.put("rdfs:label", "nameString");

            equivalentDamlCycTerms.put("daml:comment", "comment");
            equivalentDamlCycTerms.put("rdfs:comment", "comment");

            equivalentDamlCycTerms.put("daml:seeAlso", "conceptuallyRelated");
            equivalentDamlCycTerms.put("rdfs:seeAlso", "conceptuallyRelated");

            equivalentDamlCycTerms.put("daml:isDefinedBy", "containsInformationAbout");
            equivalentDamlCycTerms.put("rdfs:isDefinedBy", "containsInformationAbout");

            equivalentDamlCycTerms.put("daml:domain", "arg1Isa");
            equivalentDamlCycTerms.put("rdfs:domain", "arg1Isa");

            equivalentDamlCycTerms.put("daml:range", "arg2Isa");
            equivalentDamlCycTerms.put("rdfs:range", "arg2Isa");

            equivalentDamlCycTerms.put("daml:differentIndividualFrom", "different");

            equivalentDamlCycTerms.put("daml:samePropertyAs", "equalSymbols");

            equivalentDamlCycTerms.put("daml:disjointWith", "different");

        }
        kbSubsetCollection = cycAccess.getKnownConstantByName(kbSubsetCollectionName);
        bookkeepingMt = cycAccess.getKnownConstantByName("BookkeepingMt");
    }

    /**
     * Parses and imports the given DAML URL.
     *
     * @param damlPath the URL to import
     * @param importMtName the microtheory into which DAML content is asserted
     */
    protected void importDaml (String damlPath,
                               String importMtName)
        throws IOException, CycApiException {
        this.importMtName = importMtName;
        if (verbosity > 0)
            Log.current.println("\nImporting " + damlPath + "\ninto " + importMtName);
        importMt = cycAccess.getKnownConstantByName(importMtName);
        Log.current.println("\nStatements\n");
        //cycAccess.traceOn();
        InputStream in;
        URL url;
        try {
            File ff = new File(damlPath);
            in = new FileInputStream(ff);
            url = ff.toURL();
        }
        catch (Exception ignore) {
            try {
                url = new URL(damlPath);
                in = url.openStream();
            }
            catch (Exception e) {
                System.err.println("ARP: Failed to open: " + damlPath);
                System.err.println("    " + ParseException.formatMessage(ignore));
                System.err.println("    " + ParseException.formatMessage(e));
                return;
            }
        }
        try {
            arp.load(in, url.toExternalForm());
        }
        catch (IOException e) {
            System.err.println("Error: " + damlPath + ": " + ParseException.formatMessage(e));
        }
        catch (SAXException sax) {
            System.err.println("Error: " + damlPath + ": " + ParseException.formatMessage(sax));
        }
        if (verbosity > 0)
            Log.current.println("\nDone importing " + damlPath + "\n");
    }

    /**
     * Provides the ARP statement handler for triple having an Object.
     *
     * @param subject the RDF Triple Subject
     * @param predicate the RDF Triple Predicate
     * @param object the RDF Triple Object
     */
    public void statement(AResource subject, AResource predicate, AResource object) {
        if (subject.isAnonymous()) {
            processRestrictionSubject(subject, predicate, object);
            Log.current.println();
            return;
        }
        if (object.isAnonymous()) {
            processRestrictionObject(subject, predicate, object);
            Log.current.println();
        return;
        }
        DamlTermInfo subjectTermInfo = resource(subject, null);
        DamlTermInfo predicateTermInfo = resource(predicate, null);
        DamlTermInfo objectTermInfo = resource(object, predicateTermInfo);
        displayTriple(subjectTermInfo,
                      predicateTermInfo,
                      objectTermInfo);
        try {
            importTriple(subjectTermInfo,
                         predicateTermInfo,
                         objectTermInfo);
        }
        catch (Exception e) {
            Log.current.printStackTrace(e);
            System.exit(1);

        }
    }

    /**
     * Provides the ARP statement handler for triple having an Literal.
     *
     * @param subject the RDF Triple Subject
     * @param predicate the RDF Triple Predicate
     * @param literal the RDF Triple Literal
     */
    public void statement(AResource subject, AResource predicate, ALiteral literal) {
        if (subject.isAnonymous()) {
            processRestrictionSubject(subject, predicate, literal);
        }
        else {
            DamlTermInfo subjectTermInfo = resource(subject, null);
            DamlTermInfo predicateTermInfo = resource(predicate, null);
            DamlTermInfo literalTermInfo = literal(literal);
            displayTriple(subjectTermInfo,
                          predicateTermInfo,
                          literalTermInfo);

            try {
                importTriple(subjectTermInfo,
                             predicateTermInfo,
                             literalTermInfo);
            }
            catch (Exception e) {
                Log.current.printStackTrace(e);
                System.exit(1);

            }
        }
    }

    /**
     * Imports the RDF triple.
     *
     * @param subjectTermInfo the subject DamlTermInfo object
     * @param predicateTermInfo the predicate DamlTermInfo object
     * @param objLitTermInfo the object or literal DamlTermInfo object
     */
    protected void importTriple (DamlTermInfo subjectTermInfo,
                                  DamlTermInfo predicateTermInfo,
                                  DamlTermInfo objLitTermInfo)
        throws IOException, UnknownHostException, CycApiException {
        if (predicateTermInfo.isURI) {
            predicateTermInfo.coerceToNamespace();
        }
        String damlPredicate = predicateTermInfo.toString();
        if (damlPredicate.equals("isa")) {
            importIsa(subjectTermInfo, objLitTermInfo);
            return;
        }
        if (damlPredicate.equals("genls")) {
            importGenls(subjectTermInfo, objLitTermInfo);
            return;
        }
        else if (damlPredicate.equals("daml:versionInfo")) {
            importVersionInfo(subjectTermInfo, objLitTermInfo);
            return;
        }
        else if (damlPredicate.equals("daml:imports")) {
            importImports(subjectTermInfo, objLitTermInfo);
            return;
        }
        else if (damlPredicate.equals("comment")) {
            importComment(subjectTermInfo, objLitTermInfo);
            return;
        }
        else if (damlPredicate.equals("nameString")) {
            importNameString(subjectTermInfo, objLitTermInfo);
            return;
        }
        else if (damlPredicate.equals("equalSymbols")) {
            importEqualSymbols(subjectTermInfo, objLitTermInfo);
            return;
        }
        else if (damlPredicate.equals("arg1Isa")) {
            importArg1Isa(subjectTermInfo, objLitTermInfo);
            return;
        }
        else if (damlPredicate.equals("arg2Isa")) {
            importArg2Isa(subjectTermInfo, objLitTermInfo);
            return;
        }
        else if (damlPredicate.equals("conceptuallyRelated")) {
            importConceptuallyRelated(subjectTermInfo, objLitTermInfo);
            return;
        }
        else if (damlPredicate.equals("containsInformationAbout")) {
            importContainsInformationAbout(subjectTermInfo, objLitTermInfo);
            return;
        }
        else if (damlPredicate.equals("genlPreds")) {
            importGenlPreds(subjectTermInfo, objLitTermInfo);
            return;
        }
        else if (predicateTermInfo.ontologyNickname.equals("daml") ||
                 predicateTermInfo.ontologyNickname.equals("rdfs") ||
                 predicateTermInfo.ontologyNickname.equals("rdf")) {
            Log.current.println("\n\nUnhandled predicate: " + damlPredicate + "\n");
            return;
        }
        CycFort subject = importTerm(subjectTermInfo);
        CycFort predicate = cycAccess.getConstantByName(damlPredicate);
        if (predicate == null) {
            predicate = importTerm(predicateTermInfo);
            cycAccess.assertIsaBinaryPredicate(predicate);
        }
        if (objLitTermInfo.isLiteral) {
            cycAccess.assertGaf(importMt,
                                predicate,
                                subject,
                                (String) objLitTermInfo.literal);
            Log.current.println("(" + predicate.cyclify() + " " +
                                subject.cyclify() + " \"" +
                                (String) objLitTermInfo.literal + "\")\n");
            return;
        }
        CycFort object = cycAccess.getConstantByName(objLitTermInfo.toString());
        if (object == null)
            object = importTerm(objLitTermInfo);
        cycAccess.assertGaf(importMt,
                            predicate,
                            subject,
                            object);
        Log.current.println("(" + predicate.cyclify() + " " +
                            subject.cyclify() + " " +
                            object.cyclify() + ")\n");
    }

    /**
     * Imports the rdf:type triple.
     *
     * @param subjectTermInfo the subject DamlTermInfo object
     * @param objectTermInfo the object DamlTermInfo object
     */
    protected void importIsa (DamlTermInfo subjectTermInfo,
                              DamlTermInfo objectTermInfo)
        throws IOException, UnknownHostException, CycApiException  {
        CycFort term = importTerm(subjectTermInfo);
        String collectionName = objectTermInfo.toString();
        CycFort collection = cycAccess.getConstantByName(collectionName);
        if (collection == null) {
            Log.current.println("*** " + collectionName + " is undefined ***\n");
            collection = importTerm(objectTermInfo);
            cycAccess.assertIsaCollection(collection);
        }
        cycAccess.assertIsa(term,
                            collection);
        Log.current.println("(#$isa " +
                            term.cyclify() + " " +
                            collection.cyclify() + ")\n");
    }

    /**
     * Imports the rdf:subClassOf triple.
     *
     * @param subjectTermInfo the subject DamlTermInfo object
     * @param objectTermInfo the object DamlTermInfo object
     */
    protected void importGenls (DamlTermInfo subjectTermInfo,
                                DamlTermInfo objectTermInfo)
        throws IOException, UnknownHostException, CycApiException  {
        CycFort term = importTerm(subjectTermInfo);
        if (term == null) {
            Log.current.println("\n*** " + subjectTermInfo.toString() + " is an invalid constant ***");
            return;
        }
        String collectionName = objectTermInfo.toString();
        CycFort collection = cycAccess.getConstantByName(collectionName);
        if (collection == null) {
            Log.current.println("\n*** " + collectionName + " is undefined ***");
            collection = importTerm(objectTermInfo);
            cycAccess.assertIsaCollection(collection);
        }
        cycAccess.assertGenls(term,
                              collection);
        Log.current.println("(#$genls " +
                            term.cyclify() + " " +
                            collection.cyclify() + ")\n");
    }

    /**
     * Imports the daml:samePropertyAs triple.
     *
     * @param subjectTermInfo the subject DamlTermInfo object
     * @param objectTermInfo the object DamlTermInfo object
     */
    protected void importEqualSymbols (DamlTermInfo subjectTermInfo,
                                    DamlTermInfo objectTermInfo)
        throws IOException, UnknownHostException, CycApiException  {
        importTerm(subjectTermInfo);
        cycAccess.assertEqualSymbols(subjectTermInfo.toString(),
                                     objectTermInfo.toString());
        Log.current.println("(#$equalSymbols " +
                            subjectTermInfo.cycFort.cyclify() + " " +
                            objectTermInfo.cycFort.cyclify() + ")\n");
    }

    /**
     * Imports the rdfs:domain triple.
     *
     * @param subjectTermInfo the subject DamlTermInfo object
     * @param objectTermInfo the object DamlTermInfo object
     */
    protected void importArg1Isa (DamlTermInfo subjectTermInfo,
                                  DamlTermInfo objectTermInfo)
        throws IOException, UnknownHostException, CycApiException  {
        CycFort term1 = importTerm(subjectTermInfo);
        CycFort term2 = importTerm(objectTermInfo);
        cycAccess.assertArgIsa(term1, 1, term2);
        Log.current.println("(#$arg1Isa " +
                            term1.cyclify() + " " +
                            term2.cyclify() + ")\n");
    }

    /**
     * Imports the rdfs:range triple.
     *
     * @param subjectTermInfo the subject DamlTermInfo object
     * @param objectTermInfo the object DamlTermInfo object
     */
    protected void importArg2Isa (DamlTermInfo subjectTermInfo,
                                  DamlTermInfo objectTermInfo)
        throws IOException, UnknownHostException, CycApiException  {
        CycFort term1 = importTerm(subjectTermInfo);
        CycFort term2 = importTerm(objectTermInfo);
        cycAccess.assertArgIsa(term1, 2, term2);
        Log.current.println("(#$arg2Isa " +
                          term1.cyclify() + " " +
                          term2.cyclify() + ")\n");
    }

    /**
     * Imports the rdfs:seeAlso triple.
     *
     * @param subjectTermInfo the subject DamlTermInfo object
     * @param objectTermInfo the object DamlTermInfo object
     */
    protected void importConceptuallyRelated (DamlTermInfo subjectTermInfo,
                                              DamlTermInfo objectTermInfo)
        throws IOException, UnknownHostException, CycApiException  {
        CycFort term1 = importTerm(subjectTermInfo);
        CycFort term2 = importTerm(objectTermInfo);
        cycAccess.assertConceptuallyRelated(term1,
                                            term2,
                                            importMt);
        Log.current.println("(#$conceptuallyRelated " +
                          term1.cyclify() + " " +
                          term2.cyclify() + ")\n");
    }

    /**
     * Imports the daml:subPropertyOf triple.
     *
     * @param subjectTermInfo the subject DamlTermInfo object
     * @param objectTermInfo the object DamlTermInfo object
     */
    protected void importGenlPreds (DamlTermInfo subjectTermInfo,
                                    DamlTermInfo objectTermInfo)
        throws IOException, UnknownHostException, CycApiException  {
        CycFort specPred = importTerm(subjectTermInfo);
        CycFort genlPred = importTerm(objectTermInfo);
        cycAccess.assertGenlPreds(specPred,
                                  genlPred);
        Log.current.println("(#$genlPreds " +
                          specPred.cyclify() + " " +
                          genlPred.cyclify() + ")\n");
    }

    /**
     * Imports the daml:versionInfo triple.
     *
     * @param subjectTermInfo the subject DamlTermInfo object
     * @param literalTermInfo the object DamlTermInfo object
     */
    protected void importVersionInfo (DamlTermInfo subjectTermInfo,
                                      DamlTermInfo literalTermInfo)
        throws IOException, UnknownHostException, CycApiException {
        CycFort term = importTerm(subjectTermInfo);
        //TODO
    }

    /**
     * Imports the daml:imports triple.
     *
     * @param subjectTermInfo the subject DamlTermInfo object
     * @param literalTermInfo the object DamlTermInfo object
     */
    protected void importImports (DamlTermInfo subjectTermInfo,
                                  DamlTermInfo literalTermInfo)
        throws IOException, UnknownHostException, CycApiException {
    }

    /**
     * Imports the rdfs:comment triple.
     *
     * @param subjectTermInfo the subject DamlTermInfo object
     * @param literalTermInfo the object DamlTermInfo object
     */
    protected void importComment (DamlTermInfo subjectTermInfo,
                                  DamlTermInfo literalTermInfo)
        throws IOException, UnknownHostException, CycApiException {
        CycFort term = importTerm(subjectTermInfo);
        String comment = literalTermInfo.literalValue().replace('\n', ' ');
        cycAccess.assertComment(term,
                                comment,
                                importMt);
        Log.current.println("(#$comment " +
                          term.cyclify() + " \"" +
                          comment + "\")\n");
    }

    /**
     * Imports the rdfs:label triple.
     *
     * @param subjectTermInfo the subject DamlTermInfo object
     * @param literalTermInfo the object DamlTermInfo object
     */
    protected void importNameString (DamlTermInfo subjectTermInfo,
                                     DamlTermInfo literalTermInfo)
        throws IOException, UnknownHostException, CycApiException {
        CycFort term = importTerm(subjectTermInfo);
        cycAccess.assertNameString(term,
                                   literalTermInfo.literalValue(),
                                   importMt);
        Log.current.println("(#$nameString " +
                          term.cyclify() + " \"" +
                          literalTermInfo.literalValue() + "\")\n");
    }

    /**
     * Imports the rdfs:isDefinedBy triple.
     *
     * @param subjectTermInfo the subject DamlTermInfo object
     * @param literalTermInfo the object DamlTermInfo object
     */
    protected void importContainsInformationAbout (DamlTermInfo subjectTermInfo,
                                                   DamlTermInfo uriTermInfo)
        throws IOException, UnknownHostException, CycApiException {
        CycFort term = importTerm(subjectTermInfo);
        CycFort resource = importTerm(uriTermInfo);
        cycAccess.assertGaf(importMt,
                            cycAccess.getKnownConstantByName("containsInformationAbout"),
                            resource,
                            term);
        Log.current.println("(#$containsInformationAbout " +
                            resource.cyclify() + " " +
                            term.cyclify() + ")\n");
    }

    /**
     * Imports the given DAML term and returns the Cyc term.
     *
     * @param damlTermInfo the given daml term information
     * @return the Cyc term resulting from the import of the given DAML term
     */
    protected CycFort importTerm (DamlTermInfo damlTermInfo)
        throws IOException, UnknownHostException, CycApiException {
        if (previousDamlTermInfo != null &&
            previousDamlTermInfo.equals(damlTermInfo))
            return previousDamlTermInfo.cycFort;

        CycFort cycFort = null;
        if (damlTermInfo.isURI) {
            cycFort = new CycNart(cycAccess.getKnownConstantByName("URLFn"),
                                  damlTermInfo.toString());
            //Log.current.println("importing term: " + cycFort.cyclify());
        }
        else {
            String term = damlTermInfo.toString();
            //Log.current.println("importing term: " + term);
            try {
                cycFort = cycAccess.findOrCreate(term);
            }
            catch (CycApiException e) {
                Log.current.println("Error while importing " + term);
                Log.current.printStackTrace(e);
                System.exit(1);
            }
            if (cycFort == null)
                // error
                return cycFort;
            cycAccess.assertIsa(cycFort,
                                kbSubsetCollection,
                                bookkeepingMt);
        }
        damlTermInfo.cycFort = cycFort;
        previousDamlTermInfo = damlTermInfo;
        return cycFort;
    }

    /**
     * Displays the RDF triple.
     *
     * @param subjectTermInfo the subject DamlTermInfo object
     * @param predicateTermInfo the predicate DamlTermInfo object
     * @param objLitTermInfo the object or literal DamlTermInfo object
     */
    protected void displayTriple (DamlTermInfo subjectTermInfo,
                                  DamlTermInfo predicateTermInfo,
                                  DamlTermInfo objLitTermInfo) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(subjectTermInfo.toOriginalString());
        stringBuffer.append(" ");
        stringBuffer.append(predicateTermInfo.toOriginalString());
        stringBuffer.append(" ");
        stringBuffer.append(objLitTermInfo.toOriginalString());
        Log.current.println(stringBuffer.toString());
    }

    /**
     * Returns the DamlTerm info of the given RDF resource.
     *
     * @param aResource the RDF resource
     * @param predicateTermInfo when processing the RDF triple object,
     * contains the predicate term info, otherwise is null;
     * @return the DamlTerm info of the given RDF resource
     */
    protected DamlTermInfo resource(AResource aResource,
                                    DamlTermInfo predicateTermInfo) {
        DamlTermInfo damlTermInfo = new DamlTermInfo();
        String localName;
        String nameSpace;
        Resource resource = translateResource(aResource);
        if (aResource.isAnonymous()) {
            damlTermInfo.isAnonymous = true;
            damlTermInfo.anonymousId = aResource.getAnonymousID();
            return damlTermInfo;
        }
        else if (aResource.getURI().indexOf("?") > -1) {
            damlTermInfo.isURI = false;
            int index = aResource.getURI().indexOf("?");
            localName = aResource.getURI().substring(index + 1);
            nameSpace = aResource.getURI().substring(0, index + 1);
        }
        else if (! hasUriNamespaceSyntax(aResource.getURI())) {
            damlTermInfo.isURI = true;
            damlTermInfo.uri = resource.toString();
            if (! damlTermInfo.mustBeUri(predicateTermInfo))
                damlTermInfo.coerceToNamespace();
            return damlTermInfo;
        }
        else {
            localName = resource.getLocalName();
            nameSpace = resource.getNameSpace();
        }
        if (localName == null ||
            nameSpace == null)
            throw new RuntimeException("Invalid nameSpace " + nameSpace +
                                       " localName " + localName +
                                       " for resource " + resource.toString());
        damlTermInfo.localName = localName;
        damlTermInfo.nameSpace = nameSpace;
        String ontologyNickname = getOntologyNickname(nameSpace, resource);
        damlTermInfo.ontologyNickname = ontologyNickname;
        String constantName = ontologyNickname + ":" + localName;
        damlTermInfo.constantName = constantName;
        if (equivalentDamlCycTerms.containsKey(constantName))
            damlTermInfo.equivalentDamlCycTerm =
                (String) equivalentDamlCycTerms.get(constantName);
        return damlTermInfo;
    }

    /**
     * Returns the DamlTerm info of the given RDF literal.
     *
     * @param literal the RDF literal
     * @return the DamlTerm info of the given RDF literal
     */
    protected DamlTermInfo literal(ALiteral literal) {
        DamlTermInfo damlTermInfo = new DamlTermInfo();
        String literalString = literal.toString();
        if (this.isProbableUri(literalString)) {
            damlTermInfo.isURI = true;
            damlTermInfo.uri = literalString;
        }
        else {
            damlTermInfo.isLiteral = true;
            damlTermInfo.literal = literalString;
        }
        return damlTermInfo;
    }

    /**
     * Returns true if the given string is likely to be a URI.
     *
     * @param string the given string
     * @return true if the given string is likely to be a URI
     */
    protected boolean isProbableUri (String string) {
        return
            string.startsWith("http://") ||
            string.startsWith("https://") ||
            string.startsWith("ftp://") ||
            string.startsWith("file://") ||
            string.startsWith("urn:");
    }

    /**
     * Returns true if the given URI has embedded XML namespace separators.
     *
     * @param uri the URI
     * @return true if the given URI has embedded XML namespace separators, otherwise
     * false
     */
    protected boolean hasUriNamespaceSyntax (String uri) {
        return (uri.indexOf(":", 6) > -1) || (uri.indexOf("#") > -1);
    }

    /**
     * Records the components of a DAML Restriction.
     *
     * @param subject the RDF Triple anonymous Subject
     * @param predicate the RDF Triple Predicate
     * @param object the RDF Triple Object
     */
    protected void processRestrictionSubject(AResource subject,
                                             AResource predicate,
                                             AResource object) {
        DamlTermInfo subjectTermInfo = resource(subject, null);
        DamlTermInfo predicateTermInfo = resource(predicate, null);
        DamlTermInfo objectTermInfo = resource(object, null);
        displayTriple(subjectTermInfo,
                      predicateTermInfo,
                      objectTermInfo);
        if (predicateTermInfo.toString().equals("isa") &&
            objectTermInfo.toString().equals("daml:Restriction")) {
            damlRestriction = new DamlRestriction(this);
            damlRestriction.anonymousId = subject.getAnonymousID();
        }
        else if (damlRestriction == null) {
            // Not a Restriction, but an anonymous Class.
            try {
                importTriple(subjectTermInfo,
                             predicateTermInfo,
                             objectTermInfo);
            }
            catch (Exception e) {
                Log.current.printStackTrace(e);
                System.exit(1);
            }
            return;
        }
        else if (predicateTermInfo.toString().equals("daml:onProperty"))
            damlRestriction.property = objectTermInfo.toString();
        else if (predicateTermInfo.toString().equals("daml:toClass"))
            damlRestriction.toClasses.add(objectTermInfo.toString());
        else
            throw new RuntimeException("Unexpected restriction property " +
                                       predicateTermInfo.toString());
    }

    /**
     * Records the components of a DAML Restriction.
     *
     * @param subject the RDF Triple anonymous Subject
     * @param predicate the RDF Triple Predicate
     * @param literal the RDF Triple Literal
     */
    protected void processRestrictionSubject(AResource subject,
                                             AResource predicate,
                                             ALiteral literal) {
        DamlTermInfo subjectTermInfo = resource(subject, null);
        DamlTermInfo predicateTermInfo = resource(predicate, null);
        DamlTermInfo literalTermInfo = literal(literal);
        displayTriple(subjectTermInfo,
                      predicateTermInfo,
                      literalTermInfo);
        if (damlRestriction == null) {
            // Not a Restriction, but an anonymous Class.
            try {
                importTriple(subjectTermInfo,
                             predicateTermInfo,
                             literalTermInfo);
            }
            catch (Exception e) {
                Log.current.printStackTrace(e);
                System.exit(1);
            }
            return;
        }
        throw new RuntimeException("Unexpected restriction triple \n" +
                                   subjectTermInfo.toString() + " " +
                                   predicateTermInfo.toString() + " " +
                                   literalTermInfo.toString());
    }

    /**
     * Records the components of a DAML Restriction.
     *
     * @param subject the RDF Triple anonymous Subject
     * @param predicate the RDF Triple Predicate
     * @param object the RDF Triple Object
     */
    protected void processRestrictionObject(AResource subject,
                                            AResource predicate,
                                            AResource object) {
        DamlTermInfo subjectTermInfo = resource(subject, null);
        DamlTermInfo predicateTermInfo = resource(predicate, null);
        DamlTermInfo objectTermInfo = resource(object, null);
        displayTriple(subjectTermInfo,
                      predicateTermInfo,
                      objectTermInfo);
        if (damlRestriction == null) {
            // Not a Restriction, but an anonymous Class.
            try {
                importTriple(subjectTermInfo,
                             predicateTermInfo,
                             objectTermInfo);
            }
            catch (Exception e) {
                Log.current.printStackTrace(e);
                System.exit(1);
            }
            return;
        }
        if (predicateTermInfo.toString().equals("genls"))
            damlRestriction.fromClass = subjectTermInfo.toString();
        else
            throw new RuntimeException("Unexpected restriction property " +
                                       predicateTermInfo.toString());
        try {
            damlRestriction.formInterArgIsaConstraint();
            cycAccess.assertGaf(damlRestriction.interArgIsaConstraint,
                                importMt);
        }
        catch (Exception e) {
            Log.current.printStackTrace(e);
            System.exit(1);
        }
        Log.current.println("\n" + damlRestriction.interArgIsaConstraint.cyclify());
        damlRestriction = null;
    }

    /**
     * Returns the ontology nickname for the given XML namespace.
     *
     * @param nameSpace the XML namespace for which the nickname is sought
     * @param resource the resource containing the namespace, used for error messages
     * @return the ontology nickname for the given XML namespace
     */
    protected String getOntologyNickname (String nameSpace, Resource resource) {
        int len = nameSpace.length() - 1;
        String key = nameSpace.substring(0, len);
        String nickname = (String) ontologyNicknames.get(key);
        if (nickname == null) {
            Log.current.println("\n*** Ontology nickname not found for " + key +
                                "\nResource " + resource.toString());
            nickname = "unknown";
        }
        return nickname;
    }

    /**
     * Converts an ARP resource into a Jena resource.
     *
     * @param aResource The ARP resource.
     * @return The Jena resource.
     */
    protected Resource translateResource(AResource aResource) {
        if (aResource.isAnonymous()) {
            String id = aResource.getAnonymousID();
            Resource rr = (Resource) aResource.getUserData();
            if (rr == null) {
                rr = new ResourceImpl();
                aResource.setUserData(rr);
            }
            return rr;
        }
        else
            return new ResourceImpl(aResource.getURI());
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

    /**
     * Records the DAML term information for Cyc import.
     */
    protected class DamlTermInfo {
        boolean isAnonymous = false;
        boolean isURI = false;
        boolean isLiteral = false;
        String anonymousId;
        String nameSpace;
        String ontologyNickname;
        String localName;
        String constantName;
        String uri;
        String literal;
        String equivalentDamlCycTerm;
        CycFort cycFort;

        /**
         * Constructs a new DamlTermInfo object.
         */
        public DamlTermInfo() {
        }

        /**
         * Returns <tt>true</tt> some object equals this object
         *
         * @param object the <tt>Object</tt> for equality comparison
         * @return equals <tt>boolean</tt> value indicating equality or non-equality.
         */
        public boolean equals(Object object) {
            if (! (object instanceof DamlTermInfo))
                return false;
            return this.toString().equals(object.toString());
        }

        /**
         * Returns a non-substituted string representation of this object.
         *
         * @return a non-substituted string representation of this object
         */
        public String toOriginalString() {
            if (equivalentDamlCycTerm == null)
                return this.toString();
            else
                return constantName;
        }

        /**
         * Returns a string representation of this object.
         *
         * @return a string representation of this object
         */
        public String toString() {
            if (isAnonymous)
                return "anon-" + anonymousId;
            else if (isURI)
                return uri;
            else if (isLiteral)
                return "\"" + literal + "\"";
            else if (equivalentDamlCycTerm != null)
                return equivalentDamlCycTerm;
            else
                return constantName;
        }

        /**
         * Returns the literal value of this object.
         *
         * @return the literal value of this object
         */
        public String literalValue() {
            if (isLiteral)
                return literal;
            else
                throw new RuntimeException(this.toString() + " is not a literal");
        }

        /**
         * Coerces a namespace:localname from the URI.
         * For example, http://xmlns.com/foaf/0.1/Person -->
         * http://xmlns.com/foaf/0.1#Person
         */
        public void coerceToNamespace() {
            int index = uri.lastIndexOf("/");
            nameSpace = uri.substring(0, index);
            localName = uri.substring(index + 1);
            if (localName.equals(""))
                return;
            ontologyNickname = (String) ontologyNicknames.get(nameSpace);
            if (ontologyNickname == null) {
                Log.current.println("*** nickname not found for " + nameSpace +
                                    "\nuri " + uri);
                ontologyNickname = "unknown";
            }
            constantName = ontologyNickname + ":" + localName;
            isURI = false;
        }

        /**
         * Returns true if the uri does not represent an RDF
         * object.  Heuristic patterns are used.
         *
         * @param predicateTermInfo when present indicates that this
         * is the object of the RDF triple
         * @return true if the uri does not represent an RDF
         * object
         */
        public boolean mustBeUri(DamlTermInfo predicateTermInfo) {
            if (predicateTermInfo != null) {
                if (predicateTermInfo.toString().equals("daml:imports") ||
                    predicateTermInfo.toString().equals("rdfs:isDefinedBy") ||
                    predicateTermInfo.toString().equals("rdfs:seeAlso"))
                    return true;
            }
            if (uri.endsWith(".daml") ||
               (uri.indexOf("daml+oil") > -1))
                return true;
            return false;
        }
    }


    /**
     * Records property use restrictions which get imported
     * as Cyc interArgIsa1-2 assertions.
     */
    protected class DamlRestriction {

        /**
         * the parent ImportDaml object
         */
        ImportDaml importDaml;

        /**
         * Identifies all the RDF triples which contribute to this DAML
         * Restriction.
         */
        public String anonymousId;

        /**
         * The domain (Cyc arg1) class whose intstances are the subject of the property.
         */
        public String fromClass;

        /**
         * The property (Cyc predicate arg0) which relates the subject and predicate instances.
         */
        public String property;

        /**
         * The range (Cyc arg2) classes whose instances may be objects of the property in the
         * cases where subject is an instance of the given fromClass.
         */
        public ArrayList toClasses = new ArrayList();

        /**
         * The interArgIsa1-2 constraint sentence.
         */
        public CycList interArgIsaConstraint;

        /**
         * Constructs a new DamlRestriction object.
         */
        public DamlRestriction (ImportDaml importDaml) {
            this.importDaml = importDaml;
        }

        public CycList formInterArgIsaConstraint()
            throws IOException, UnknownHostException, CycApiException {
            if (toClasses.size() == 1) {
                String interArgIsaConstraintString =
                    "(#$interArgIsa1-2 #$" + property +
                    " #$" + fromClass +
                    " #$" + toClasses.get(0) + ")";
                interArgIsaConstraint = cycAccess.makeCycList(interArgIsaConstraintString);
            }
            else
                //TODO
                throw new RuntimeException("Unhandled restriction case: " +
                                           toClasses.toString());
            return interArgIsaConstraint;
        }

        /**
         * Returns a string representation of this object.
         *
         * @return a string representation of this object
         */
        public String toString() {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("anon-");
            stringBuffer.append(anonymousId);
            stringBuffer.append(": ");
            stringBuffer.append(fromClass);
            stringBuffer.append(" ");
            stringBuffer.append(property);
            stringBuffer.append(" (");
            for (int i = 0; i < toClasses.size(); i++) {
                if (i > 0)
                    stringBuffer.append(", ");
                stringBuffer.append(toClasses.get(i).toString());
            }
            stringBuffer.append(") ");
            if (interArgIsaConstraint != null)
                stringBuffer.append(interArgIsaConstraint.cyclify());
            return stringBuffer.toString();
        }
    }

}