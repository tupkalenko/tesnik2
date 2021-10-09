import base.Factory;
import base.JsonGenerator;
import base.Person;

import java.io.IOException;
import java.nio.file.Files;

public class Main {
    public static void main(String[] args) throws IOException {
        Factory factory = new Factory();
        Person person = new Person(1l, "a", "b");

        JsonGenerator<Person> jsonGenerator = factory.createJsonGenerator(Person.class);
        System.out.println(String.join("", Files.readAllLines(jsonGenerator.generateAndGetFile(person).toPath())));
    }
}
