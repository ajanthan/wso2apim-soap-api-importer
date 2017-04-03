package org.wso2.apim.tools;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.io.FileUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * This is a command line utility to create WSO2 API definition for existing SOAP APIs
 */
public class App {
    public static void main(String[] args) {
        OptionParser parser = new OptionParser();
        parser.accepts("workingDir").withRequiredArg().required();
        parser.accepts("apiName").withRequiredArg().required();
        parser.accepts("apiVersion").withRequiredArg().required();
        parser.accepts("provider").withRequiredArg().required();
        parser.accepts("context").withRequiredArg().required();
        parser.accepts("description").withRequiredArg().required();
        parser.accepts("technicalOwner").withRequiredArg().required();
        parser.accepts("technicalOwnerEmail").withRequiredArg().required();
        parser.accepts("businessOwner").withRequiredArg().required();
        parser.accepts("businessOwnerEmail").withRequiredArg().required();
        parser.accepts("productionEndpoint").withRequiredArg().required();
        parser.accepts("sandboxEndpoints").withRequiredArg().required();
        parser.accepts("wsdl").withRequiredArg().required();
        parser.accepts("tags").withRequiredArg().required().withValuesSeparatedBy(',');
        parser.accepts("debug");
        OptionSet optionSet = parser.parse(args);

        String workingDirectory = (String) optionSet.valueOf("workingDir");
        String apiName = (String) optionSet.valueOf("apiName");
        ;
        String apiVersion = (String) optionSet.valueOf("apiVersion");
        String provider = (String) optionSet.valueOf("provider");
        String apiContext = (String) optionSet.valueOf("context");
        String description = (String) optionSet.valueOf("description");
        String technicalOwner = (String) optionSet.valueOf("technicalOwner");
        String technicalOwnerEmail = (String) optionSet.valueOf("technicalOwnerEmail");
        String businessOwner = (String) optionSet.valueOf("businessOwner");
        String businessOwnerEmail = (String) optionSet.valueOf("businessOwnerEmail");
        String productionEndpoint = (String) optionSet.valueOf("productionEndpoint");
        String sandboxEndpoints = (String) optionSet.valueOf("sandboxEndpoints");
        String wsdl = (String) optionSet.valueOf("wsdl");
        List<String> tags = (List<String>) optionSet.valuesOf("tags");
        boolean debug = optionSet.hasArgument("debug");
        File zipDir = new File(workingDirectory + File.separator + apiName + "-" + apiVersion);
        zipDir.mkdir();


        /*  first, get and initialize an engine  */
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();

        /*  add that list to a VelocityContext  */
        VelocityContext context = new VelocityContext();
        context.put("name", apiName);
        context.put("provider", provider);
        context.put("context", apiContext);
        context.put("version", apiVersion);
        context.put("description", description);
        context.put("technicalOwner", technicalOwner);
        context.put("technicalOwnerEmail", technicalOwnerEmail);
        context.put("businessOwner", businessOwner);
        context.put("businessOwnerEmail", businessOwnerEmail);
        context.put("productionEndpoint", productionEndpoint);
        context.put("sandboxEndpoints", sandboxEndpoints);
        context.put("tags", tags);

        Template apiJsonTemplate = ve.getTemplate("api.json.vm");
        File metaInfoDir = new File(zipDir.getAbsolutePath() + File.separator + "Meta-information");
        metaInfoDir.mkdir();
        /*  now render the template into a Writer  */
        FileWriter writer = null;
        try {
            writer = new FileWriter(new File(metaInfoDir.getAbsolutePath() + File.separator + "api.json"));
        } catch (IOException e) {
            System.err.println("Error in generating api.json");
            printDebugLog(e, debug);
        }
        apiJsonTemplate.merge(context, writer);
        /* use the output in your email body */
        try {
            writer.close();
        } catch (IOException e) {
            printDebugLog(e, debug);
        }
        System.out.println("Successfully generated api.json");
        Template swaggerJsonTemplate = ve.getTemplate("swagger.json.vm");

        FileWriter swaggerWriter = null;
        try {
            swaggerWriter = new FileWriter(new File(metaInfoDir.getAbsolutePath() + File.separator + "swagger.json"));
        } catch (IOException e) {
            System.err.println("Error in generating swagger.json");
            printDebugLog(e, debug);
        }
        swaggerJsonTemplate.merge(context, swaggerWriter);
        /* use the output in your email body */
        try {
            swaggerWriter.close();
        } catch (IOException e) {
            printDebugLog(e, debug);
        }
        System.out.println("Successfully generated swagger.json");
        File wsdlDir = new File(zipDir.getAbsolutePath() + File.separator + "WSDL");
        wsdlDir.mkdir();
        File wsdlFile = new File(wsdlDir.getAbsolutePath() + File.separator + apiName + "-" + apiVersion + ".wsdl");
        try {

            wsdlFile.createNewFile();
        } catch (IOException e) {
            printDebugLog(e, debug);
        }
        File wsdlSource = new File(wsdl);
        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(wsdlSource).getChannel();
            destination = new RandomAccessFile(wsdlFile, "rws").getChannel();

            destination.transferFrom(source, 0, source.size());

        } catch (FileNotFoundException e) {
            System.err.println("WSDL file is not found");
            printDebugLog(e, debug);
        } catch (IOException e) {
            System.err.println("Error in copying ");
            printDebugLog(e, debug);
        } finally {
            try {
                source.close();
            } catch (IOException e) {
                printDebugLog(e, debug);
            }
            try {
                destination.close();
            } catch (IOException e) {
                printDebugLog(e, debug);
            }
        }
        System.out.println("Successfully copied WSDL");
        try {
            ZipUtil.archiveDirectory(zipDir.toString());
        } catch (Exception e) {
            System.err.println("Error in creating zip file");
            printDebugLog(e, debug);
        } finally {
            try {
                FileUtils.deleteDirectory(zipDir);
            } catch (IOException e) {
                printDebugLog(e, debug);
            }
        }
        System.out.println("Successfully generated API archive");
    }

    private static void printDebugLog(Exception e, boolean debug) {
        if (debug) {
            e.printStackTrace();
        }
    }
}
