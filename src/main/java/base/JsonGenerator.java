package base;

import java.io.File;

public interface JsonGenerator<R> {

   File generateAndGetFile(R object);
}
