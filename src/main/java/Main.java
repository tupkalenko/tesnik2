import base.*;

import java.io.IOException;
import java.nio.file.Files;

public class Main {
    public static void main(String[] args) throws IOException {
        XMLFactory factory = new XMLFactory();
        Person person = new Person(1l, "a", "b");

         Generator<Person> generator = factory.createGenerator(Person.class);
        System.out.println(String.join("", Files.readAllLines(generator.generateAndGetFile(person).toPath())));
    }
}
