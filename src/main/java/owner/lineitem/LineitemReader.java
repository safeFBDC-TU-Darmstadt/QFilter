package owner.lineitem;

import common.BatchedReader;

public class LineitemReader extends BatchedReader {

    public LineitemReader() {
        super(LineitemAttributes.filePath);
    }

}
