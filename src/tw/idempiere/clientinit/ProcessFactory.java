package tw.idempiere.clientinit;

import org.adempiere.base.IProcessFactory;
import org.compiere.process.ProcessCall;

import tw.idempiere.clientinit.process.DeleteClientProcess;
import tw.idempiere.clientinit.process.InitializeClientPreview;
import tw.idempiere.clientinit.process.InitializeClientProcess;

/**
 * Process Factory for Client Initialize Plugin
 *
 * 負責建立此插件的 Process 實例
 *
 * @author iDempiere Community
 */
public class ProcessFactory implements IProcessFactory {

    @Override
    public ProcessCall newProcessInstance(String className) {
        // Initialize Client Process
        if (InitializeClientProcess.class.getName().equals(className)) {
            return new InitializeClientProcess();
        }
        // Initialize Client Preview Process
        if (InitializeClientPreview.class.getName().equals(className)) {
            return new InitializeClientPreview();
        }
        // Delete Client Process
        if (DeleteClientProcess.class.getName().equals(className)) {
            return new DeleteClientProcess();
        }
        return null;
    }
}
