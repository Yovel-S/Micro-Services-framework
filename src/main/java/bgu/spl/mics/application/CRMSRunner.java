package bgu.spl.mics.application;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;

import java.io.*;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.concurrent.*;


import bgu.spl.mics.application.services.ConferenceService;
import bgu.spl.mics.application.services.TimeService;
import com.google.gson.*;

/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {
    public static JsonElement getJsonTree(File file) {
        JsonParser parser = new JsonParser();
        try {
            return parser.parse(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Student[] getStudents(JsonObject je) {
        JsonArray json_students = je.getAsJsonArray("Students");
        Student[] students = new Student[json_students.size()];
        JsonObject student;
        JsonElement student_name;
        JsonElement department;
        JsonElement status;
        JsonObject model;
        JsonElement model_name;
        JsonElement model_type;
        JsonElement model_size;
        JsonArray json_models;
        for (int i = 0; i < students.length; i++) {
            student = json_students.get(i).getAsJsonObject();
            student_name = student.get("name");
            department = student.get("department");
            status = student.get("status");
            json_models = student.get("models").getAsJsonArray();
            Model[] models = new Model[json_models.size()];
            Gson gson = new Gson();
            for (int j = 0; j < models.length; j++) {
                model = json_models.get(j).getAsJsonObject();
                model_name = model.get("name");
                model_type = model.get("type");
                model_size = model.get("size");
                models[j] = new Model(model_name.getAsString(),model_type.getAsString(),model_size.getAsInt());
            }
            students[i] = new Student(student_name.getAsString(), department.getAsString(), status.getAsString(), models);
        }
        return students;
    }

    public static GPU[] getGPUs(JsonObject je, Cluster cluster) {
        JsonArray json_GPUs = je.getAsJsonArray("GPUS");
        GPU[] GPUs = new GPU[json_GPUs.size()];
        Gson gson = new Gson();
        String type;
        for (int j = 0; j < GPUs.length; j++) {
            type = json_GPUs.get(j).getAsString();
            GPUs[j] = new GPU(type, cluster);
        }
        return GPUs;
    }

    public static CPU[] getCPUs(JsonObject je, Cluster cluster) {
        JsonArray json_CPUs = je.getAsJsonArray("CPUS");
        CPU[] CPUs = new CPU[json_CPUs.size()];
        Gson gson = new Gson();
        int cores;
        for (int j = 0; j < CPUs.length; j++) {
            cores = json_CPUs.get(j).getAsInt();
            CPUs[j] = new CPU(cores, cluster);
        }
        return CPUs;
    }

    public static ConfrenceInformation[] getConfrences(JsonObject je) {
        JsonArray ja = je.getAsJsonArray("Conferences");
        ConfrenceInformation[] confrences = new ConfrenceInformation[ja.size()];
        JsonElement name;
        JsonElement date;
        JsonObject confrence;
        JsonArray json_models;
        for (int i = 0; i < confrences.length; i++) {
            confrence = ja.get(i).getAsJsonObject();
            name = confrence.get("name");
            date = confrence.get("date");
            confrences[i] = new ConfrenceInformation(name.getAsString(),date.getAsInt());
        }
        return confrences;
    }

    public static void main(String[] args) {
        int tickTime;
        int duration;
        TimeService timeService;
        GPUService[] GPUservices;
        CPUService[] CPUservices;
        StudentService[] studentServices;
        ConferenceService[] conferenceServices;

        /** Building the Objects from json file **/
        final Cluster cluster = Cluster.getInstance();
        Gson gson = new Gson();
        File file=null;
        if(args.length > 0)
            file = new File(args[0]);
        JsonElement jsonTree = getJsonTree(file);
        JsonObject je = jsonTree.getAsJsonObject();
        Student[] students = getStudents(je);
        GPU[] GPUs = getGPUs(je, cluster);
        CPU[] CPUs = getCPUs(je, cluster);
        ConfrenceInformation[] confrences = getConfrences(je);
        tickTime = je.getAsJsonPrimitive("TickTime").getAsInt();
        duration = je.getAsJsonPrimitive("Duration").getAsInt();

        /**@parm (numOfThreads) will be used for synchronize the start&termination of the system.**/
        int numOfThreads=students.length+ GPUs.length+ CPUs.length+ confrences.length+1;
        /** start countdown -in order to determine when all threads (except timeService) has been initialized. **/
        CountDownLatch startThreadCount = new CountDownLatch(numOfThreads-1);
        /** end countdown - in order to determine when all the threads has been terminated.*/
        CountDownLatch endThreadCount = new CountDownLatch(numOfThreads);

        /**Building the MicroServices "TimeService" and one for each Object**/
        timeService = new TimeService(duration, tickTime, endThreadCount);
        GPUservices = new GPUService[GPUs.length];
        for(int i=0;i< GPUservices.length;i++){
            GPUservices[i] = new GPUService(String.valueOf(i), GPUs[i], startThreadCount, endThreadCount);
        }
        CPUservices = new CPUService[CPUs.length];
        for(int i=0;i< CPUservices.length;i++){
            CPUservices[i] = new CPUService(String.valueOf(i), CPUs[i], startThreadCount, endThreadCount);
        }
        studentServices = new StudentService[students.length];
        for(int i=0;i< studentServices.length;i++){
            studentServices[i] = new StudentService(String.valueOf(i), students[i], startThreadCount, endThreadCount);
        }
        conferenceServices = new ConferenceService[confrences.length];
        for(int i=0;i< conferenceServices.length;i++){
            conferenceServices[i] = new ConferenceService("conferenceService " +String.valueOf(i),confrences[i], startThreadCount, endThreadCount);
        }
        cluster.setGPUsCPUs(GPUs, CPUs);

        /** @param executor ExecutorService Object that will run the threads(services). **/
        ExecutorService executor = Executors.newFixedThreadPool(numOfThreads);

        /** Executing all threads(services). **/
        for(GPUService service: GPUservices)
            executor.execute(service);
        for(CPUService service: CPUservices)
            executor.execute(service);
        for(StudentService service: studentServices)
            executor.execute(service);
        for(ConferenceService service: conferenceServices)
            executor.execute(service);

        /** Executing TimeService when other threads(services) initialized. **/
        try {
            startThreadCount.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.execute(timeService);
        /** Waiting for all threads(services) to terminate. **/
        try {
            endThreadCount.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdown();

        Gson gsonOutput = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT).setPrettyPrinting().create();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream("output.json");
            String jsonOut="{\n\"Students\":\n";
            jsonOut += gsonOutput.toJson(students)+",";
            jsonOut += "\n\"Confrences\":\n";
            jsonOut += gsonOutput.toJson(confrences)+",";
            jsonOut += "\n\"Statistics\":\n";
            jsonOut += gsonOutput.toJson(cluster);
            jsonOut += "\n}";
            fos.write(jsonOut.getBytes());
            fos.close();

        } catch (IOException e) {}
    }


}
