package org.example.logger.util;

public final class LineNumberUitl {

    // Every class of the library lives under this package.
    private static final String LIBRARY_PACKAGE = "org.example.logger.";

    private static final StackWalker WALKER = StackWalker.getInstance();

    private LineNumberUitl() {
    }

    // Line number in the app that called log.info(...) etc., or 0 if not found.
    // Walks up the current call stack and skips the library's own frames;
    // the first frame left is the app's line.
    public static int find() {
        return WALKER.walk(frames -> frames
                .filter(frame -> !frame.getClassName().startsWith(LIBRARY_PACKAGE))
                .findFirst()
                .map(StackWalker.StackFrame::getLineNumber)
                .orElse(0));
    }
}
