import java.io.IOException;

class ExamException extends Exception {
    ExamException(String m) { super(m); }
    ExamException(String m, Throwable c) { super(m, c); }
}
class WindowClosedException extends ExamException {
    WindowClosedException(String m) { super(m); }
}
class NotRegisteredException extends ExamException {
    NotRegisteredException(String m) { super(m); }
}
class SubmissionStorageException extends ExamException {
    SubmissionStorageException(String m, Throwable c) { super(m, c); }
}

class StorageLayer {
    void store(String file, long kb) throws SubmissionStorageException {
        try {
            if (kb > 500) throw new IOException("Disk quota exceeded");
            System.out.println("  [storage] saved " + file);
        } catch (IOException e) {
            throw new SubmissionStorageException("Could not store " + file, e);
        }
    }
}
class ServiceLayer {
    final StorageLayer storage = new StorageLayer();
    boolean chainCause = true;
    void submit(String student, boolean registered, boolean windowOpen, String file, long kb) throws ExamException {
        if (!windowOpen) throw new WindowClosedException("Exam window closed");
        if (!registered) throw new NotRegisteredException(student + " is not registered");
        try {
            storage.store(file, kb);
        } catch (SubmissionStorageException e) {
            if (chainCause) throw new SubmissionStorageException("Submission failed for " + student, e); // keeps cause
            else throw new SubmissionStorageException("Submission failed for " + student, null);       // cause omitted
        }
    }
}
class Controller {
    final ServiceLayer service = new ServiceLayer();
    String handle(String student, boolean reg, boolean open, String file, long kb) {
        try {
            service.submit(student, reg, open, file, kb);
            return "OK: submission accepted";
        } catch (WindowClosedException e) {
            return "REJECT: " + e.getMessage() + " | cause=" + e.getCause();
        } catch (NotRegisteredException e) {
            return "REJECT: " + e.getMessage() + " | cause=" + e.getCause();
        } catch (ExamException e) {
            return "STORAGE FAIL: " + e.getMessage() + " | cause=" + e.getCause();
        }
    }
}

public class Layers {
    public static void main(String[] args) throws Exception {
        Controller c = new Controller();
        System.out.println("== Test table ==");
        System.out.println("1 valid         -> " + c.handle("S1", true, true, "a.pdf", 100));
        System.out.println("2 window closed -> " + c.handle("S2", true, false, "a.pdf", 100));
        System.out.println("3 not registered-> " + c.handle("S3", false, true, "a.pdf", 100));
        System.out.println("4 size > 500 KB -> " + c.handle("S4", true, true, "big.pdf", 800));

        System.out.println("\n== getCause() chain (cause kept) ==");
        try { c.service.submit("S4", true, true, "big.pdf", 800); }
        catch (ExamException e) {
            Throwable t = e; int depth = 0;
            while (t != null) { System.out.println("  level " + depth++ + ": " + t.getClass().getName() + ": " + t.getMessage()); t = t.getCause(); }
            System.out.println("\n== Full chained stack trace ==");
            e.printStackTrace(System.out);
        }

        System.out.println("\n== Cause omitted ==");
        c.service.chainCause = false;
        try { c.service.submit("S4", true, true, "big.pdf", 800); }
        catch (ExamException e) {
            System.out.println("  getMessage() = " + e.getMessage());
            System.out.println("  getCause()   = " + e.getCause());
            e.printStackTrace(System.out);
            try { System.out.println("  e.getCause().getMessage() -> " + e.getCause().getMessage()); }
            catch (NullPointerException npe) { System.out.println("  e.getCause().getMessage() -> NullPointerException: " + npe.getMessage()); }
        }
    }
}
