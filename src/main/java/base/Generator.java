package base;

import java.io.File;

public interface Generator<R> {

   File generateAndGetFile(R object);

}
