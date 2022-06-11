import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.checkerframework.dataflow.qual.TerminatesExecution;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.IOException;
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;

public class SampleClient {

    public static List<Long> sol = new ArrayList<>();
    public static void main(String[] theArgs) {

        // Create a FHIR client
        FhirContext fhirContext = FhirContext.forR4();
        IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
        client.registerInterceptor(new LoggingInterceptor(false));

        // Search for Patient resources
        Bundle response = client
                .search()
                .forResource("Patient")
                .where(Patient.FAMILY.matches().value("SMITH"))
                .returnBundle(Bundle.class)
                .execute();
       
        List<Patient> patients = collectPatientDataFromResponse(response);
        System.out.println("\n---------Patient Details(First Name , Last Name and Date of Birth) --------------");
        System.out.printf("%-20s  %-20s %s \n", "FIRST NAME", "LAST NAME", "BIRTH DATE");
        printPatientDetails(patients);    // print data of patients with labels

        /* sort the patient data */
        patients.sort(new SortByFirstName());

        System.out.println("\n---------Patient data Sorted by First Name --------------");
        System.out.printf("%-20s  %-20s %s \n", "FIRST NAME", "LAST NAME", "BIRTH DATE");
        // print data of patients with labels
        printPatientDetails(patients);

        clientInterceptor doubleans = new clientInterceptor();
        clientInterceptor doubleans1 = new clientInterceptor();

        client.registerInterceptor(doubleans);
        client.registerInterceptor(doubleans1);    
        
        List<String> Mainsol = readLastName();
        long add = 0;

        // Getting the response from every last name and measuring the time for its response time
        for(int i=0; i< Mainsol.size();i++){
            customLastName(client,Mainsol.get(i));
            add += doubleans.finalSol().get(i);
            
        }

        long avg = add/20;
        // Getting the response from every last name and measuring the average response. 
        for(int i=0; i< Mainsol.size();i++){
            customLastName(client,Mainsol.get(i));
            add += doubleans.finalSol().get(i);
        }
        long avg2 = add/40;
        

        System.out.println("Average of 20 searches is : " + avg);
        System.out.println("Average of 20 searches is : " + avg2);
        
        long add2 = 0;
        // Measuring the average response by disabaling the cache memory
        for(int i=0; i< Mainsol.size();i++){
            customLastName1(client, Mainsol.get(i));
            add2 += doubleans1.finalSol().get(i);
        }

        System.out.println("Average time of 20 searches by disabaling the cache is : " + add2/20);
    }

    // Getting the response for each of the lastName
    public static Bundle customLastName(IGenericClient client, String lastName) {
    
            Bundle response = client
                .search()
                .forResource("Patient")
                .where(Patient.FAMILY.matches().value(lastName))
                .returnBundle(Bundle.class)
                .execute();
            
        return response;
    }

    // Getting the response for each of the lastName by disabaling the cache memory
    public static Bundle customLastName1 (IGenericClient client, String lastName) {
    
        Bundle response = client
            .search()
            .forResource("Patient")
            .where(Patient.FAMILY.matches().value(lastName))
            .returnBundle(Bundle.class)
            .cacheControl(new CacheControlDirective().setNoCache(true))
            .execute();
        
    return response;
    
    }
    /**
     * extracts the patients details from bundle and returns as a List
     * @param bundle bundle data containing patient data
     * @return List of patients fetched from the bundle
     */
    public static List<Patient> collectPatientDataFromResponse(Bundle bundle) {
        List<Patient> patients = new ArrayList<>();
        for (Bundle.BundleEntryComponent be : bundle.getEntry()) {
            if (be.getResource() instanceof Patient) { //safe check to avoid casting errors
                patients.add((Patient) be.getResource());
            }
        }
        return patients;
    }

    /**
     * print the basic details(name and birth date) of patient
     * @param data patients data in form of List collection
     */
    public static void printPatientDetails(List<Patient> data) {
        for (Patient patient : data) {
            HumanName name = patient.getName().get(0);
            Date birthDate = patient.getBirthDate();
            // print first and last name of patient and print date of birth IF available in standard format
            System.out.printf("%-20s  %-20s %s \n", name.getGiven().get(0).toString(), name.getFamily(), (birthDate != null) ? new SimpleDateFormat("yyyy-MM-dd").format(birthDate) : "N/A");
        }
    }
    // Reading all the last Name from the LastName text file and putting all them together in a List
    public static List<String>  readLastName(){
        List<String> ans = new ArrayList<>();
        try {
            File myObj = new File("LastName.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
              String data = myReader.nextLine();
                ans.add(data);
            }
            myReader.close();
          } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
          }
        return ans;  
    }
}

class clientInterceptor implements IClientInterceptor {
    /**
     * Fired by the client just before invoking the HTTP client request
     */
    List<Long> temp = new ArrayList<>();
    @Override
    public void interceptRequest (IHttpRequest theRequest){
        return;
    }
    
    /**
     * Fired by the client upon receiving an HTTP response, prior to processing that response
     */
    @Override
    public void interceptResponse(IHttpResponse theResponse) throws IOException{
        temp.add(theResponse.getRequestStopWatch().getMillis());
        return;
    };

    public List<Long> finalSol(){
        return temp;
    }

}
/**
 * Comparator class to sort the patient details by First Name
 * To be used with List.sort() method
 * This is for Task 2 of basic tasks
 */
class SortByFirstName implements Comparator<Patient> {
    public int compare(Patient patient1, Patient patient2) {
        String name1 = patient1.getName().get(0).getGiven().get(0).toString();
        String name2 = patient2.getName().get(0).getGiven().get(0).toString();
        return name1.compareToIgnoreCase(name2);
    }
}