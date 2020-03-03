import java.io.InputStream;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.VCARD;

public class Main {
	
	public static String onto = "http://climate.linkeddata.es/jaen/clima#";
	public static String xsd = "http://www.w3.org/2001/XMLSchema#";
	public static String ssn = "http://purl.oclc.org/NET/ssnx/ssn#";
	public static String rdfs = "http://www.w3.org/2000/01/rdf-schema#";
	public static String schema = "http://schema.org/";
	public static String owl = 	"http://www.w3.org/2002/07/owl#";
	

	public static void main(String[] args) {
		String filename = "dataset/JaenAgroclimatica.rdf";
		
		// Create an empty model
		Model model = ModelFactory.createDefaultModel();
		
		// Use the FileManager to find the input file
		InputStream in = FileManager.get().open(filename);

		if (in == null)
			throw new IllegalArgumentException("File: "+filename+" not found");

		// Read the RDF/XML file
		model.read(in, null);
		
		// Reused variables
		String queryString;
		Query query;
		QueryExecution qexec;
		ResultSet results;

		// Query 1: Extract the names of all locations and their DBpedia reconciled URIs
		
		queryString =
				"PREFIX ssn: <" + ssn + "> " +
				"PREFIX schema: <" + schema + "> " +
				"PREFIX rdfs: <" + rdfs  + "> " +
				"PREFIX owl: <" + owl + "> " +
				"SELECT DISTINCT ?DBLocation ?Name " +
				"WHERE { ?Measurement ssn:onPlatform ?Platform. " +
				"?Platform schema:containedIn ?Location. " +
				"?Location rdfs:label ?Name. " +
				"?Location owl:sameAs ?DBLocation. }";
				
		query = QueryFactory.create(queryString);
		qexec = QueryExecutionFactory.create(query, model) ;
		results = qexec.execSelect() ;
		
		while (results.hasNext())
		{
			QuerySolution binding = results.nextSolution();
			Resource dblocation = (Resource) binding.get("DBLocation");
			Literal name = binding.getLiteral("Name");
			System.out.printf("Name: %-30s\tDBpedia: %s%n", name, dblocation.getURI());
		}
		
		// Query 2: Extract all the measurements of the months of July and August of 2005 of station 2
		
		queryString =
				"PREFIX onto: <" + onto + "> " +
				"PREFIX xsd: <" + xsd + "> " +
				"PREFIX rdfs: <" + rdfs + "> " +
				"PREFIX ssn: <" + ssn + "> " +
				"SELECT ?Measurement " +
				"WHERE { ?Measurement ssn:observationSamplingTime ?Date. " +
				"?Measurement ssn:onPlatform ?Platform. " +
				"?Platform rdfs:label ?Label. " +
			    "FILTER (year(?Date) = 2005 && month(?Date) > 6 && month(?Date) < 9 " +
				"&& ?Label = \'2\') }" +
			    "ORDER BY ?Measurement";
		
		query = QueryFactory.create(queryString);
		qexec = QueryExecutionFactory.create(query, model) ;
		results = qexec.execSelect() ;
		
		while (results.hasNext())
		{
			//System.out.println("There are results");
			QuerySolution binding = results.nextSolution();
			Resource subj = (Resource) binding.get("Measurement");
			System.out.println(subj.getURI());
		}
		
		// Query 3: Extract all measurements whose maximum registered temperature was over 40 degrees,
		// as well as their average temperatures
		
		queryString =
				"PREFIX onto: <" + onto + "> " +
				"PREFIX xsd: <" + xsd + "> " +
				"PREFIX rdfs: <" + rdfs + "> " +
				"PREFIX ssn: <" + ssn + "> " +
				"SELECT ?Measurement ?AvgTemp " +
				"WHERE { ?Measurement onto:hasMaxTemp ?MaxTemp. " +
				"?Measurement onto:hasAverageTemp ?AvgTemp. " +
			    "FILTER (?MaxTemp > 40.0) }";
		
		query = QueryFactory.create(queryString);
		qexec = QueryExecutionFactory.create(query, model) ;
		results = qexec.execSelect() ;
		
		while (results.hasNext())
		{
			//System.out.println("There are results");
			QuerySolution binding = results.nextSolution();
			Resource subj = (Resource) binding.get("Measurement");
			Literal temp = binding.getLiteral("AvgTemp");
			System.out.println(subj.getURI() + ": " + temp.getDouble());		
		}
		
		// Query 4: Find the day that rained the most
		
		queryString =
				"PREFIX onto: <" + onto + "> " +
				"PREFIX ssn: <" + ssn + "> " +
				"SELECT ?Date ?Precip " +
				"WHERE { ?Measurement onto:hasRain ?Precip. " +
				"?Measurement ssn:observationSamplingTime ?Date. }" +
			    "ORDER BY DESC(?Precip) " +
				"LIMIT 1";
		
		query = QueryFactory.create(queryString);
		qexec = QueryExecutionFactory.create(query, model) ;
		results = qexec.execSelect() ;
		
		while (results.hasNext())
		{
			QuerySolution binding = results.nextSolution();
			Literal date = binding.getLiteral("Date");
			Literal precip = binding.getLiteral("Precip");
			System.out.println(date.getString() + " - Rain amount: " + precip.getDouble());		
		}
		
		// Query 5: Extracts the minimum temperature ever registered, alongside its date and location.
		
		queryString =
				"PREFIX onto: <" + onto + "> " +
				"PREFIX rdfs: <" + rdfs + "> " +
				"PREFIX ssn: <" + ssn + "> " +
				"PREFIX schema: <" + schema + "> " +
				"SELECT ?Date ?Temp ?PlaceName " +
				"WHERE { ?Measurement onto:hasMinTemp ?Temp. " +
				"?Measurement onto:hasMinTempTime ?Date. " +
				"?Measurement ssn:onPlatform ?Station. " +
				"?Station schema:containedIn ?Place. " +
				"?Place rdfs:label ?PlaceName. }" +
			    "ORDER BY ?Temp " +
				"LIMIT 1";
		
		query = QueryFactory.create(queryString);
		qexec = QueryExecutionFactory.create(query, model) ;
		results = qexec.execSelect() ;
		
		while (results.hasNext())
		{
			QuerySolution binding = results.nextSolution();
			Literal date = binding.getLiteral("Date");
			Literal temp = binding.getLiteral("Temp");
			Literal place = binding.getLiteral("PlaceName");
			System.out.println("The minimum temperature ever registered is " + temp.getDouble() + " on " + date.getString() + " at " + place.getString());		
		}
		
		// Query 6: Extracts the highest temperature registered in the station located at Huesa alongside its date and time
		
		queryString =
				"PREFIX onto: <" + onto + "> " +
				"PREFIX rdfs: <" + rdfs + "> " +
				"PREFIX ssn: <" + ssn + "> " +
				"PREFIX schema: <" + schema + "> " +
				"SELECT ?Date ?Temp " +
				"WHERE { ?Measurement onto:hasMaxTemp ?Temp. " +
				"?Measurement onto:hasMaxTempTime ?Date. " +
				"?Measurement ssn:onPlatform ?Station. " +
				"?Station schema:containedIn ?Place. " +
				"?Place rdfs:label ?PlaceName. " +
				"FILTER (?PlaceName = \'Huesa\') } " +
			    "ORDER BY DESC(?Temp) " +
				"LIMIT 1";
		
		query = QueryFactory.create(queryString);
		qexec = QueryExecutionFactory.create(query, model) ;
		results = qexec.execSelect() ;
		
		while (results.hasNext())
		{
			QuerySolution binding = results.nextSolution();
			Literal date = binding.getLiteral("Date");
			Literal temp = binding.getLiteral("Temp");
			System.out.println("Highest temperature registered at Huesa is " + temp.getDouble() + " on date " + date.getString());		
		}
		
	}

}
