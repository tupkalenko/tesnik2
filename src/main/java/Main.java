import base.*;

import java.io.IOException;
import java.nio.file.Files;

public class Main {
    public static void main(String[] args) throws IOException {
        Person person = new Person(1l, "a", "b");

        XMLFactory xmlFactory = new XMLFactory();
        XmlGenerator<Person> xmlGenerator = xmlFactory.createGenerator(Person.class);

        JsonFactory jsonFactory = new JsonFactory();
        JsonGenerator<Person> jsonGenerator = jsonFactory.createGenerator(Person.class);

        System.out.println(String.join("", Files.readAllLines(jsonGenerator.generateAndGetFile(person).toPath())));
        System.out.println(String.join("", Files.readAllLines(xmlGenerator.generateAndGetFile(person).toPath())));
    }
}
