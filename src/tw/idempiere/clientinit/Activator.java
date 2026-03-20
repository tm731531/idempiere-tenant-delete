package tw.idempiere.clientinit;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * iDempiere Client Initialize Plugin Activator
 *
 * 此 Plugin 用於初始化 Tenant 的交易資料，保留主資料結構。
 *
 * @author iDempiere Community
 */
public class Activator implements BundleActivator {

    private static BundleContext context;

    /**
     * 取得 BundleContext
     * @return BundleContext
     */
    static BundleContext getContext() {
        return context;
    }

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
        System.out.println("tw.idempiere.clientinit plugin started");
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        Activator.context = null;
        System.out.println("tw.idempiere.clientinit plugin stopped");
    }
}
