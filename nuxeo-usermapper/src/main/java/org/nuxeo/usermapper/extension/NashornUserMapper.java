package org.nuxeo.usermapper.extension;

import java.io.Serializable;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.automation.scripting.internals.ScriptingCache;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;

public class NashornUserMapper extends AbstractUserMapper {

    protected static final Log log = LogFactory.getLog(NashornUserMapper.class);

    protected ScriptEngine engine;

    protected final String mapperSource;

    protected final String wrapperSource;

    public NashornUserMapper(String mapperScript, String wrapperScript) {
        super();
        mapperSource = mapperScript;
        wrapperSource = wrapperScript;
    }

    @Override
    public Object wrapNuxeoPrincipal(NuxeoPrincipal principal, Object userObject, Map<String, Serializable> params) {
        if (StringUtils.isEmpty(wrapperSource)) {
            return null;
        }
        Bindings bindings = new SimpleBindings();
        bindings.put("nuxeoPrincipal", principal);
        bindings.put("userObject", userObject);
        bindings.put("params", params);
        try {
            engine.eval(wrapperSource, bindings);
        } catch (ScriptException e) {
            log.error("Error while executing JavaScript mapper", e);
        }
        return bindings.get("userObject");
    }

    @Override
    public void init(Map<String, String> params) throws Exception {
        ScriptingCache scripting = new ScriptingCache(true);
        engine = scripting.getScriptEngine();
    }

    @Override
    public void release() {
        // NOP
    }

    @Override
    protected void resolveAttributes(Object userObject, Map<String, Serializable> searchAttributes,
            Map<String, Serializable> userAttributes, Map<String, Serializable> profileAttributes) {
        Bindings bindings = new SimpleBindings();
        bindings.put("searchAttributes", searchAttributes);
        bindings.put("profileAttributes", profileAttributes);
        bindings.put("userAttributes", userAttributes);
        bindings.put("userObject", userObject);

        try {
            engine.eval(mapperSource, bindings);
        } catch (ScriptException e) {
            log.error("Error while executing JavaScript mapper", e);
        }
    }

}
