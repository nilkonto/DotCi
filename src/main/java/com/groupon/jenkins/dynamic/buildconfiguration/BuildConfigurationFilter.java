/*
The MIT License (MIT)

Copyright (c) 2014, Groupon, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
package com.groupon.jenkins.dynamic.buildconfiguration;

import groovy.text.GStringTemplateEngine;
import hudson.EnvVars;

import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.google.common.collect.ForwardingMap;

public class BuildConfigurationFilter {

	private final String config;
	private final EnvVars envVars;

	public BuildConfigurationFilter(String config, EnvVars envVars) {
		this.config = config;
		this.envVars = new EnvVars(envVars);
		this.envVars.remove("PATH");
	}

	public Map getConfig() {
		GStringTemplateEngine engine = new GStringTemplateEngine();
		Object template = null;
		try {
			template = engine.createTemplate(config).make(new MissingPropForwardingMap(envVars));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return (Map) new Yaml().load(template.toString());
	}

	private static class MissingPropForwardingMap extends ForwardingMap<String, String> {

		private final EnvVars delegate;

		public MissingPropForwardingMap(EnvVars envVars) {
			this.delegate = envVars;
		}

		@Override
		public boolean containsKey(Object key) {
			return true;
		}

		@Override
		public String get(Object key) {
			String value = super.get(key);
			return value == null ? "$" + key : value;
		}

		@Override
		protected Map<String, String> delegate() {
			return delegate;
		}

	}
}
