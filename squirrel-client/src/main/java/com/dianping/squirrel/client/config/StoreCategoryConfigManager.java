/**
 * Project: avatar
 * 
 * File Created at 2010-10-15
 * $Id$
 * 
 * Copyright 2010 Dianping.com Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Dianping.com.
 */
package com.dianping.squirrel.client.config;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.squirrel.client.config.zookeeper.CacheCuratorClient;
import com.dianping.squirrel.client.util.DTOUtils;
import com.dianping.squirrel.common.domain.CacheKeyConfigurationDTO;
import com.dianping.squirrel.common.exception.StoreException;

import net.sf.ehcache.CacheException;

/**
 * Remote centralized managed cache item config
 * 
 * @author danson.liu
 * 
 */
public class StoreCategoryConfigManager {

	private static transient Logger logger = LoggerFactory.getLogger(StoreCategoryConfigManager.class);

	private CacheCuratorClient cacheCuratorClient = CacheCuratorClient.getInstance();

	private ConcurrentMap<String, StoreCategoryConfig> cacheKeyTypes = new ConcurrentHashMap<String, StoreCategoryConfig>();

	private Set<String> usedCategories = new ConcurrentSkipListSet<String>();

//	private ConfigManager configManager = ConfigManagerLoader.getConfigManager();

	private static StoreCategoryConfigManager instance;

	private List<StoreCategoryConfigListener> configListeners;

	private StoreCategoryConfigManager() {
		try {
			init();
		} catch (Exception e) {
			logger.error("failed to init cache item config manager", e);
		}
	}

	public static StoreCategoryConfigManager getInstance() {
		if (instance == null) {
			synchronized (StoreClientConfigManager.class) {
				if (instance == null) {
					instance = new StoreCategoryConfigManager();
				}
			}
		}
		return instance;
	}

	public synchronized void addConfigListener(StoreCategoryConfigListener listener) {
		checkNotNull(listener, "category config listener is null");
		if (configListeners == null) {
			configListeners = new ArrayList<StoreCategoryConfigListener>();
		}
		configListeners.add(listener);
	}

	private void fireConfigChanged(StoreCategoryConfig categoryConfig) {
		if (configListeners != null) {
			for (StoreCategoryConfigListener listener : configListeners) {
				try {
					listener.configChanged(categoryConfig);
				} catch (Throwable t) {
					logger.error("failed to notify category config change: " + categoryConfig, t);
				}
			}
		}
	}

	private void fireConfigRemoved(StoreCategoryConfig categoryConfig) {
		if (configListeners != null) {
			for (StoreCategoryConfigListener listener : configListeners) {
				try {
					listener.configRemoved(categoryConfig);
				} catch (Throwable t) {
					logger.error("failed to notify category config remove: " + categoryConfig, t);
				}
			}
		}
	}

	public StoreCategoryConfig getCacheKeyType(String category) {
		return cacheKeyTypes.get(category);
	}

	public StoreCategoryConfig init(String category) {
		StoreCategoryConfig cacheKeyType = cacheKeyTypes.get(category);
		if (cacheKeyType == null) {
			synchronized (this) {
				cacheKeyType = cacheKeyTypes.get(category);
				if (cacheKeyType == null) {
					CacheKeyConfigurationDTO categoryConfig;
					try {
						categoryConfig = loadCategoryConfig(category);
					} catch (Exception e) {
						throw new StoreException("failed to load category config: " + category, e);
					}
					if (categoryConfig == null) {
						logger.error("category config is null: " + category);
						Cat.logError(new CacheException("category config is null: " + category));
						StoreCategoryConfig defaultType = new DefaultStoreCategoryConfig(category);
						cacheKeyTypes.put(category, defaultType);
						return defaultType;
					}
					logger.info("loaded category config: " + categoryConfig);
					cacheKeyType = registerCacheKey(categoryConfig);
				}
			}
		}
		return cacheKeyType;
	}

	public StoreCategoryConfig findCacheKeyType(String category) {
		if (StringUtils.isBlank(category)) {
			throw new NullPointerException("store category is empty");
		}
		if (!usedCategories.contains(category)) {
			usedCategories.add(category);
		}
		return init(category);
	}

	private CacheKeyConfigurationDTO loadCategoryConfig(String category) throws Exception {
		if(logger.isDebugEnabled()){
			logger.debug("loading category config from zookeeper: " + category);
		}
		return cacheCuratorClient.getCategoryConfig(category);
	}

	/**
	 * @param configurationDTO
	 */
	public void updateConfig(CacheKeyConfigurationDTO configurationDTO) {
		StoreCategoryConfig categoryConfig = registerCacheKey(configurationDTO);
		fireConfigChanged(categoryConfig);
	}

	/**
	 * @param configurationDTO
	 */
	private StoreCategoryConfig registerCacheKey(CacheKeyConfigurationDTO configurationDTO) {
		StoreCategoryConfig cacheKeyType = new StoreCategoryConfig();
		DTOUtils.copyProperties(cacheKeyType, configurationDTO);
		cacheKeyTypes.put(cacheKeyType.getCategory(), cacheKeyType);
		return cacheKeyType;
	}

	public Set<String> getCacheItemKeys() {
		return usedCategories;
	}

	public void removeCacheKeyType(String category) {
		StoreCategoryConfig categoryConfig = cacheKeyTypes.remove(category);
		if (categoryConfig != null) {
			fireConfigRemoved(categoryConfig);
		}
	}

	public void init() throws Exception {
		initCacheCategories();
	}

	private void initCacheCategories() {
//		if (PathUtils.isZookeeperEnabled()) {
//			String appName = configManager.getAppName();
//			if (StringUtils.isNotEmpty(appName)) {
//				try {
//					String categories = cacheCuratorClient.getRuntimeCategories(appName);
//					if (StringUtils.isNotEmpty(categories)) {
//						logger.info("initializing cache categories: " + categories);
//						String[] cacheCategories = StringUtils.split(categories, ',');
//						for (String cacheCategory : cacheCategories) {
//							if (StringUtils.isNotBlank(cacheCategory)) {
//								init(cacheCategory.trim());
//							}
//						}
//					}
//				} catch (Exception e) {
//					logger.error("failed to initialize cache categories", e);
//				}
//			}
//		}
	}
}
