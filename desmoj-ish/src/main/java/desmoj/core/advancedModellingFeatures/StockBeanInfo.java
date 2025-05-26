package desmoj.core.advancedModellingFeatures;

import java.lang.reflect.Method;

/**
 * The bean information class for desmoj.Stock.
 *
 * @version DESMO-J, Ver. 2.5.1d copyright (c) 2015
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
public class StockBeanInfo extends java.beans.SimpleBeanInfo {
    /**
     * Find the method by comparing (name & parameter size) against the methods in the class.
     *
     * @param aClass         java.lang.Class
     * @param methodName     java.lang.String
     * @param parameterCount int
     * @return java.lang.reflect.Method
     */
    public static java.lang.reflect.Method findMethod(Class<?> aClass, java.lang.String methodName,
                                                      int parameterCount) {
        try {
            /*
             * Since this method attempts to find a method by getting all methods from the
             * class, this method should only be called if getMethod cannot find the method.
             */
            java.lang.reflect.Method methods[] = aClass.getMethods();
            for (Method method : methods) {
                if ((method.getParameterTypes().length == parameterCount) && (method.getName().equals(methodName))) {
                    return method;
                }
            }
        } catch (java.lang.Throwable exception) {
            return null;
        }
        return null;
    }

    /**
     * Gets the bean class.
     *
     * @return java.lang.Class
     */
    public static java.lang.Class<Stock> getBeanClass() {
        return desmoj.core.advancedModellingFeatures.Stock.class;
    }

    /**
     * Gets the bean class name.
     *
     * @return java.lang.String
     */
    public static java.lang.String getBeanClassName() {
        return Stock.class.getName();
    }

    /**
     * Gets the avail property descriptor.
     *
     * @return java.beans.PropertyDescriptor
     */
    public java.beans.PropertyDescriptor availPropertyDescriptor() {
        java.beans.PropertyDescriptor aDescriptor = null;
        try {
            try {
                /*
                 * Using methods via getMethod is the faster way to create the avail property
                 * descriptor.
                 */
                java.lang.reflect.Method aGetMethod = null;
                try {
                    /*
                     * Attempt to find the method using getMethod with parameter types.
                     */
                    Class<?> aGetMethodParameterTypes[] = {};
                    aGetMethod = getBeanClass().getMethod("getAvail", aGetMethodParameterTypes);
                } catch (Throwable exception) {
                    /* Since getMethod failed, call findMethod. */
                    handleException(exception);
                    aGetMethod = findMethod(getBeanClass(), "getAvail", 0);
                }
                java.lang.reflect.Method aSetMethod = null;
                aDescriptor = new java.beans.PropertyDescriptor("avail", aGetMethod, aSetMethod);
            } catch (Throwable exception) {
                /*
                 * Since we failed using methods, try creating a default property descriptor.
                 */
                handleException(exception);
                aDescriptor = new java.beans.PropertyDescriptor("avail", getBeanClass());
            }
            aDescriptor.setBound(true);
            /* aDescriptor.setConstrained(false); */
            aDescriptor.setDisplayName("availableProducts");
            aDescriptor.setShortDescription("The number of available products in the Stock at the moment.");
            /* aDescriptor.setExpert(false); */
            /* aDescriptor.setHidden(false); */
            /* aDescriptor.setValue("preferred", new Boolean(false)); */
            /* aDescriptor.setValue("ivjDesignTimeProperty", new Boolean(true)); */
        } catch (Throwable exception) {
            handleException(exception);
        }
        return aDescriptor;
    }

    /**
     * Gets the avgAvail() method descriptor.
     *
     * @return java.beans.MethodDescriptor
     */
    public java.beans.MethodDescriptor avgAvailMethodDescriptor() {
        java.beans.MethodDescriptor aDescriptor = null;
        try {
            /* Create and return the avgAvail() method descriptor. */
            java.lang.reflect.Method aMethod = null;
            try {
                /*
                 * Attempt to find the method using getMethod with parameter types.
                 */
                Class<?> aParameterTypes[] = {};
                aMethod = getBeanClass().getMethod("avgAvail", aParameterTypes);
            } catch (Throwable exception) {
                /* Since getMethod failed, call findMethod. */
                handleException(exception);
                aMethod = findMethod(getBeanClass(), "avgAvail", 0);
            }
            try {
                /*
                 * Try creating the method descriptor with parameter descriptors.
                 */
                java.beans.ParameterDescriptor aParameterDescriptors[] = {};
                aDescriptor = new java.beans.MethodDescriptor(aMethod, aParameterDescriptors);
            } catch (Throwable exception) {
                /*
                 * Try creating the method descriptor without parameter descriptors.
                 */
                handleException(exception);
                aDescriptor = new java.beans.MethodDescriptor(aMethod);
            }
            aDescriptor.setDisplayName("AverageAvailableUnits");
            aDescriptor.setShortDescription("The average number of available units in the stock");
            /* aDescriptor.setExpert(false); */
            /* aDescriptor.setHidden(false); */
            /* aDescriptor.setValue("preferred", new Boolean(false)); */
        } catch (Throwable exception) {
            handleException(exception);
        }
        return aDescriptor;
    }

    /**
     * Gets the capacity property descriptor.
     *
     * @return java.beans.PropertyDescriptor
     */
    public java.beans.PropertyDescriptor capacityPropertyDescriptor() {
        java.beans.PropertyDescriptor aDescriptor = null;
        try {
            try {
                /*
                 * Using methods via getMethod is the faster way to create the capacity property
                 * descriptor.
                 */
                java.lang.reflect.Method aGetMethod = null;
                try {
                    /*
                     * Attempt to find the method using getMethod with parameter types.
                     */
                    Class<?> aGetMethodParameterTypes[] = {};
                    aGetMethod = getBeanClass().getMethod("getCapacity", aGetMethodParameterTypes);
                } catch (Throwable exception) {
                    /* Since getMethod failed, call findMethod. */
                    handleException(exception);
                    aGetMethod = findMethod(getBeanClass(), "getCapacity", 0);
                }
                java.lang.reflect.Method aSetMethod = null;
                aDescriptor = new java.beans.PropertyDescriptor("capacity", aGetMethod, aSetMethod);
            } catch (Throwable exception) {
                /*
                 * Since we failed using methods, try creating a default property descriptor.
                 */
                handleException(exception);
                aDescriptor = new java.beans.PropertyDescriptor("capacity", getBeanClass());
            }
            /* aDescriptor.setBound(false); */
            /* aDescriptor.setConstrained(false); */
            /* aDescriptor.setDisplayName("capacity"); */
            aDescriptor.setShortDescription("The maximum number of products this stock can hold");
            /* aDescriptor.setExpert(false); */
            /* aDescriptor.setHidden(false); */
            /* aDescriptor.setValue("preferred", new Boolean(false)); */
            /* aDescriptor.setValue("ivjDesignTimeProperty", new Boolean(true)); */
        } catch (Throwable exception) {
            handleException(exception);
        }
        return aDescriptor;
    }

    /**
     * Gets the consumers property descriptor.
     *
     * @return java.beans.PropertyDescriptor
     */
    public java.beans.PropertyDescriptor consumersPropertyDescriptor() {
        java.beans.PropertyDescriptor aDescriptor = null;
        try {
            try {
                /*
                 * Using methods via getMethod is the faster way to create the consumers
                 * property descriptor.
                 */
                java.lang.reflect.Method aGetMethod = null;
                try {
                    /*
                     * Attempt to find the method using getMethod with parameter types.
                     */
                    Class<?> aGetMethodParameterTypes[] = {};
                    aGetMethod = getBeanClass().getMethod("getConsumers", aGetMethodParameterTypes);
                } catch (Throwable exception) {
                    /* Since getMethod failed, call findMethod. */
                    handleException(exception);
                    aGetMethod = findMethod(getBeanClass(), "getConsumers", 0);
                }
                java.lang.reflect.Method aSetMethod = null;
                aDescriptor = new java.beans.PropertyDescriptor("consumers", aGetMethod, aSetMethod);
            } catch (Throwable exception) {
                /*
                 * Since we failed using methods, try creating a default property descriptor.
                 */
                handleException(exception);
                aDescriptor = new java.beans.PropertyDescriptor("consumers", getBeanClass());
            }
            /* aDescriptor.setBound(false); */
            /* aDescriptor.setConstrained(false); */
            /* aDescriptor.setDisplayName("consumers"); */
            aDescriptor.setShortDescription("The number of consumers having received products from this stock");
            /* aDescriptor.setExpert(false); */
            /* aDescriptor.setHidden(false); */
            /* aDescriptor.setValue("preferred", new Boolean(false)); */
            /* aDescriptor.setValue("ivjDesignTimeProperty", new Boolean(true)); */
        } catch (Throwable exception) {
            handleException(exception);
        }
        return aDescriptor;
    }

    /**
     * Gets the createDefaultReporter() method descriptor.
     *
     * @return java.beans.MethodDescriptor
     */
    public java.beans.MethodDescriptor createReporterMethodDescriptor() {
        java.beans.MethodDescriptor aDescriptor = null;
        try {
            /* Create and return the createDefaultReporter() method descriptor. */
            java.lang.reflect.Method aMethod = null;
            try {
                /*
                 * Attempt to find the method using getMethod with parameter types.
                 */
                Class<?> aParameterTypes[] = {};
                aMethod = getBeanClass().getMethod("createReporter", aParameterTypes);
            } catch (Throwable exception) {
                /* Since getMethod failed, call findMethod. */
                handleException(exception);
                aMethod = findMethod(getBeanClass(), "createReporter", 0);
            }
            try {
                /*
                 * Try creating the method descriptor with parameter descriptors.
                 */
                java.beans.ParameterDescriptor aParameterDescriptors[] = {};
                aDescriptor = new java.beans.MethodDescriptor(aMethod, aParameterDescriptors);
            } catch (Throwable exception) {
                /*
                 * Try creating the method descriptor without parameter descriptors.
                 */
                handleException(exception);
                aDescriptor = new java.beans.MethodDescriptor(aMethod);
            }
            /* aDescriptor.setDisplayName("createDefaultReporter()"); */
            aDescriptor.setShortDescription("returns a reporter reporting about this Stock");
            /* aDescriptor.setExpert(false); */
            /* aDescriptor.setHidden(false); */
            /* aDescriptor.setValue("preferred", new Boolean(false)); */
        } catch (Throwable exception) {
            handleException(exception);
        }
        return aDescriptor;
    }

    /**
     * Returns the BeanInfo of the superclass of this bean to inherit its features.
     *
     * @return java.beans.BeanInfo[]
     */
    @Override
    public java.beans.BeanInfo[] getAdditionalBeanInfo() {
        Class<?> superClass;
        java.beans.BeanInfo superBeanInfo = null;

        try {
            superClass = getBeanDescriptor().getBeanClass().getSuperclass();
        } catch (java.lang.Throwable exception) {
            return null;
        }

        try {
            superBeanInfo = java.beans.Introspector.getBeanInfo(superClass);
        } catch (java.beans.IntrospectionException ie) {
        }

        if (superBeanInfo != null) {
            java.beans.BeanInfo[] ret = new java.beans.BeanInfo[1];
            ret[0] = superBeanInfo;
            return ret;
        }
        return null;
    }

    @Override
    public java.beans.BeanDescriptor getBeanDescriptor() {
        java.beans.BeanDescriptor aDescriptor = null;
        try {
            /* Create and return the StockBeanInfo bean descriptor. */
            aDescriptor = new java.beans.BeanDescriptor(desmoj.core.advancedModellingFeatures.Stock.class);
            /* aDescriptor.setExpert(false); */
            /* aDescriptor.setHidden(false); */
            /* aDescriptor.setValue("hidden-state", Boolean.FALSE); */
        } catch (Throwable exception) {
        }
        return aDescriptor;
    }

    /**
     * Gets the getConsQueueStrategy() method descriptor.
     *
     * @return java.beans.MethodDescriptor
     */
    public java.beans.MethodDescriptor getConsQueueStrategyMethodDescriptor() {
        java.beans.MethodDescriptor aDescriptor = null;
        try {
            /* Create and return the getConsQueueStrategy() method descriptor. */
            java.lang.reflect.Method aMethod = null;
            try {
                /*
                 * Attempt to find the method using getMethod with parameter types.
                 */
                Class<?> aParameterTypes[] = {};
                aMethod = getBeanClass().getMethod("getConsQueueStrategy", aParameterTypes);
            } catch (Throwable exception) {
                /* Since getMethod failed, call findMethod. */
                handleException(exception);
                aMethod = findMethod(getBeanClass(), "getConsQueueStrategy", 0);
            }
            try {
                /*
                 * Try creating the method descriptor with parameter descriptors.
                 */
                java.beans.ParameterDescriptor aParameterDescriptors[] = {};
                aDescriptor = new java.beans.MethodDescriptor(aMethod, aParameterDescriptors);
            } catch (Throwable exception) {
                /*
                 * Try creating the method descriptor without parameter descriptors.
                 */
                handleException(exception);
                aDescriptor = new java.beans.MethodDescriptor(aMethod);
            }
            aDescriptor.setDisplayName("getConsumerQueueStrategy");
            aDescriptor.setShortDescription("gets the strategy of the consumer queue as an integer");
            /* aDescriptor.setExpert(false); */
            /* aDescriptor.setHidden(false); */
            /* aDescriptor.setValue("preferred", new Boolean(false)); */
        } catch (Throwable exception) {
            handleException(exception);
        }
        return aDescriptor;
    }

    /**
     * Gets the getConsRefused() method descriptor.
     *
     * @return java.beans.MethodDescriptor
     */
    public java.beans.MethodDescriptor getConsRefusedMethodDescriptor() {
        java.beans.MethodDescriptor aDescriptor = null;
        try {
            /* Create and return the getConsRefused() method descriptor. */
            java.lang.reflect.Method aMethod = null;
            try {
                /*
                 * Attempt to find the method using getMethod with parameter types.
                 */
                Class<?> aParameterTypes[] = {};
                aMethod = getBeanClass().getMethod("getConsRefused", aParameterTypes);
            } catch (Throwable exception) {
                /* Since getMethod failed, call findMethod. */
                handleException(exception);
                aMethod = findMethod(getBeanClass(), "getConsRefused", 0);
            }
            try {
                /*
                 * Try creating the method descriptor with parameter descriptors.
                 */
                java.beans.ParameterDescriptor aParameterDescriptors[] = {};
                aDescriptor = new java.beans.MethodDescriptor(aMethod, aParameterDescriptors);
            } catch (Throwable exception) {
                /*
                 * Try creating the method descriptor without parameter descriptors.
                 */
                handleException(exception);
                aDescriptor = new java.beans.MethodDescriptor(aMethod);
            }
            aDescriptor.setDisplayName("getConsumersRefused");
            aDescriptor.setShortDescription(
            "get the number of consumers refused to be enqueued because consumer queue was full");
            /* aDescriptor.setExpert(false); */
            /* aDescriptor.setHidden(false); */
            /* aDescriptor.setValue("preferred", new Boolean(false)); */
        } catch (Throwable exception) {
            handleException(exception);
        }
        return aDescriptor;
    }

    /**
     * Return the event set descriptors for this bean.
     *
     * @return java.beans.EventSetDescriptor[]
     */
    @Override
    public java.beans.EventSetDescriptor[] getEventSetDescriptors() {
        try {
            java.beans.EventSetDescriptor aDescriptorList[] = {};
            return aDescriptorList;
        } catch (Throwable exception) {
            handleException(exception);
        }
        return null;
    }

    /**
     * Return the method descriptors for this bean.
     *
     * @return java.beans.MethodDescriptor[]
     */
    @Override
    public java.beans.MethodDescriptor[] getMethodDescriptors() {
        try {
            java.beans.MethodDescriptor aDescriptorList[] = { avgAvailMethodDescriptor(),
                                                              createReporterMethodDescriptor(),
                                                              getConsQueueStrategyMethodDescriptor(),
                                                              getConsRefusedMethodDescriptor(),
                                                              getProdQueueStrategyMethodDescriptor(),
                                                              getProdRefusedMethodDescriptor(),
                                                              getProducerQueueMethodDescriptor(),
                                                              resetMethodDescriptor(), retrieve_longMethodDescriptor(),
                                                              store_longMethodDescriptor() };
            return aDescriptorList;
        } catch (Throwable exception) {
            handleException(exception);
        }
        return null;
    }

    /**
     * Gets the getProdQueueStrategy() method descriptor.
     *
     * @return java.beans.MethodDescriptor
     */
    public java.beans.MethodDescriptor getProdQueueStrategyMethodDescriptor() {
        java.beans.MethodDescriptor aDescriptor = null;
        try {
            /* Create and return the getProdQueueStrategy() method descriptor. */
            java.lang.reflect.Method aMethod = null;
            try {
                /*
                 * Attempt to find the method using getMethod with parameter types.
                 */
                Class<?> aParameterTypes[] = {};
                aMethod = getBeanClass().getMethod("getProdQueueStrategy", aParameterTypes);
            } catch (Throwable exception) {
                /* Since getMethod failed, call findMethod. */
                handleException(exception);
                aMethod = findMethod(getBeanClass(), "getProdQueueStrategy", 0);
            }
            try {
                /*
                 * Try creating the method descriptor with parameter descriptors.
                 */
                java.beans.ParameterDescriptor aParameterDescriptors[] = {};
                aDescriptor = new java.beans.MethodDescriptor(aMethod, aParameterDescriptors);
            } catch (Throwable exception) {
                /*
                 * Try creating the method descriptor without parameter descriptors.
                 */
                handleException(exception);
                aDescriptor = new java.beans.MethodDescriptor(aMethod);
            }
            aDescriptor.setDisplayName("getProducerQueueStrategy");
            aDescriptor.setShortDescription("gets the strategy of the producer queue as an integer");
            /* aDescriptor.setExpert(false); */
            /* aDescriptor.setHidden(false); */
            /* aDescriptor.setValue("preferred", new Boolean(false)); */
        } catch (Throwable exception) {
            handleException(exception);
        }
        return aDescriptor;
    }

    /**
     * Gets the getProdRefused() method descriptor.
     *
     * @return java.beans.MethodDescriptor
     */
    public java.beans.MethodDescriptor getProdRefusedMethodDescriptor() {
        java.beans.MethodDescriptor aDescriptor = null;
        try {
            /* Create and return the getProdRefused() method descriptor. */
            java.lang.reflect.Method aMethod = null;
            try {
                /*
                 * Attempt to find the method using getMethod with parameter types.
                 */
                Class<?> aParameterTypes[] = {};
                aMethod = getBeanClass().getMethod("getProdRefused", aParameterTypes);
            } catch (Throwable exception) {
                /* Since getMethod failed, call findMethod. */
                handleException(exception);
                aMethod = findMethod(getBeanClass(), "getProdRefused", 0);
            }
            try {
                /*
                 * Try creating the method descriptor with parameter descriptors.
                 */
                java.beans.ParameterDescriptor aParameterDescriptors[] = {};
                aDescriptor = new java.beans.MethodDescriptor(aMethod, aParameterDescriptors);
            } catch (Throwable exception) {
                /*
                 * Try creating the method descriptor without parameter descriptors.
                 */
                handleException(exception);
                aDescriptor = new java.beans.MethodDescriptor(aMethod);
            }
            aDescriptor.setDisplayName("getProducersRefused");
            aDescriptor.setShortDescription("get the number of producers refused to enqueue, because queue was full");
            /* aDescriptor.setExpert(false); */
            /* aDescriptor.setHidden(false); */
            /* aDescriptor.setValue("preferred", new Boolean(false)); */
        } catch (Throwable exception) {
            handleException(exception);
        }
        return aDescriptor;
    }

    /**
     * Gets the getProducerQueue() method descriptor.
     *
     * @return java.beans.MethodDescriptor
     */
    public java.beans.MethodDescriptor getProducerQueueMethodDescriptor() {
        java.beans.MethodDescriptor aDescriptor = null;
        try {
            /* Create and return the getProducerQueue() method descriptor. */
            java.lang.reflect.Method aMethod = null;
            try {
                /*
                 * Attempt to find the method using getMethod with parameter types.
                 */
                Class<?> aParameterTypes[] = {};
                aMethod = getBeanClass().getMethod("getProducerQueue", aParameterTypes);
            } catch (Throwable exception) {
                /* Since getMethod failed, call findMethod. */
                handleException(exception);
                aMethod = findMethod(getBeanClass(), "getProducerQueue", 0);
            }
            try {
                /*
                 * Try creating the method descriptor with parameter descriptors.
                 */
                java.beans.ParameterDescriptor aParameterDescriptors[] = {};
                aDescriptor = new java.beans.MethodDescriptor(aMethod, aParameterDescriptors);
            } catch (Throwable exception) {
                /*
                 * Try creating the method descriptor without parameter descriptors.
                 */
                handleException(exception);
                aDescriptor = new java.beans.MethodDescriptor(aMethod);
            }
            /* aDescriptor.setDisplayName("getProducerQueue()"); */
            aDescriptor.setShortDescription("get the queue where producers are waiting to deliver their units");
            /* aDescriptor.setExpert(false); */
            /* aDescriptor.setHidden(false); */
            /* aDescriptor.setValue("preferred", new Boolean(false)); */
        } catch (Throwable exception) {
            handleException(exception);
        }
        return aDescriptor;
    }

    /**
     * Return the property descriptors for this bean.
     *
     * @return java.beans.PropertyDescriptor[]
     */
    @Override
    public java.beans.PropertyDescriptor[] getPropertyDescriptors() {
        try {
            java.beans.PropertyDescriptor aDescriptorList[] = { availPropertyDescriptor(), capacityPropertyDescriptor(),
                                                                consumersPropertyDescriptor(),
                                                                initialPropertyDescriptor(),
                                                                maximumPropertyDescriptor(),
                                                                minimumPropertyDescriptor(),
                                                                producersPropertyDescriptor(),
                                                                refusedPropertyDescriptor() };
            return aDescriptorList;
        } catch (Throwable exception) {
            handleException(exception);
        }
        return null;
    }

    /**
     * Gets the initial property descriptor.
     *
     * @return java.beans.PropertyDescriptor
     */
    public java.beans.PropertyDescriptor initialPropertyDescriptor() {
        java.beans.PropertyDescriptor aDescriptor = null;
        try {
            try {
                /*
                 * Using methods via getMethod is the faster way to create the initial property
                 * descriptor.
                 */
                java.lang.reflect.Method aGetMethod = null;
                try {
                    /*
                     * Attempt to find the method using getMethod with parameter types.
                     */
                    Class<?> aGetMethodParameterTypes[] = {};
                    aGetMethod = getBeanClass().getMethod("getInitial", aGetMethodParameterTypes);
                } catch (Throwable exception) {
                    /* Since getMethod failed, call findMethod. */
                    handleException(exception);
                    aGetMethod = findMethod(getBeanClass(), "getInitial", 0);
                }
                java.lang.reflect.Method aSetMethod = null;
                aDescriptor = new java.beans.PropertyDescriptor("initial", aGetMethod, aSetMethod);
            } catch (Throwable exception) {
                /*
                 * Since we failed using methods, try creating a default property descriptor.
                 */
                handleException(exception);
                aDescriptor = new java.beans.PropertyDescriptor("initial", getBeanClass());
            }
            /* aDescriptor.setBound(false); */
            /* aDescriptor.setConstrained(false); */
            /* aDescriptor.setDisplayName("initial"); */
            aDescriptor.setShortDescription("The initial number of products the Stock starts with");
            /* aDescriptor.setExpert(false); */
            /* aDescriptor.setHidden(false); */
            /* aDescriptor.setValue("preferred", new Boolean(false)); */
            /* aDescriptor.setValue("ivjDesignTimeProperty", new Boolean(true)); */
        } catch (Throwable exception) {
            handleException(exception);
        }
        return aDescriptor;
    }

    /**
     * Gets the maximum property descriptor.
     *
     * @return java.beans.PropertyDescriptor
     */
    public java.beans.PropertyDescriptor maximumPropertyDescriptor() {
        java.beans.PropertyDescriptor aDescriptor = null;
        try {
            try {
                /*
                 * Using methods via getMethod is the faster way to create the maximum property
                 * descriptor.
                 */
                java.lang.reflect.Method aGetMethod = null;
                try {
                    /*
                     * Attempt to find the method using getMethod with parameter types.
                     */
                    Class<?> aGetMethodParameterTypes[] = {};
                    aGetMethod = getBeanClass().getMethod("getMaximum", aGetMethodParameterTypes);
                } catch (Throwable exception) {
                    /* Since getMethod failed, call findMethod. */
                    handleException(exception);
                    aGetMethod = findMethod(getBeanClass(), "getMaximum", 0);
                }
                java.lang.reflect.Method aSetMethod = null;
                aDescriptor = new java.beans.PropertyDescriptor("maximum", aGetMethod, aSetMethod);
            } catch (Throwable exception) {
                /*
                 * Since we failed using methods, try creating a default property descriptor.
                 */
                handleException(exception);
                aDescriptor = new java.beans.PropertyDescriptor("maximum", getBeanClass());
            }
            /* aDescriptor.setBound(false); */
            /* aDescriptor.setConstrained(false); */
            /* aDescriptor.setDisplayName("maximum"); */
            aDescriptor.setShortDescription("The maximum number of products in the Stock");
            /* aDescriptor.setExpert(false); */
            /* aDescriptor.setHidden(false); */
            /* aDescriptor.setValue("preferred", new Boolean(false)); */
            /* aDescriptor.setValue("ivjDesignTimeProperty", new Boolean(true)); */
        } catch (Throwable exception) {
            handleException(exception);
        }
        return aDescriptor;
    }

    /**
     * Gets the minimum property descriptor.
     *
     * @return java.beans.PropertyDescriptor
     */
    public java.beans.PropertyDescriptor minimumPropertyDescriptor() {
        java.beans.PropertyDescriptor aDescriptor = null;
        try {
            try {
                /*
                 * Using methods via getMethod is the faster way to create the minimum property
                 * descriptor.
                 */
                java.lang.reflect.Method aGetMethod = null;
                try {
                    /*
                     * Attempt to find the method using getMethod with parameter types.
                     */
                    Class<?> aGetMethodParameterTypes[] = {};
                    aGetMethod = getBeanClass().getMethod("getMinimum", aGetMethodParameterTypes);
                } catch (Throwable exception) {
                    /* Since getMethod failed, call findMethod. */
                    handleException(exception);
                    aGetMethod = findMethod(getBeanClass(), "getMinimum", 0);
                }
                java.lang.reflect.Method aSetMethod = null;
                aDescriptor = new java.beans.PropertyDescriptor("minimum", aGetMethod, aSetMethod);
            } catch (Throwable exception) {
                /*
                 * Since we failed using methods, try creating a default property descriptor.
                 */
                handleException(exception);
                aDescriptor = new java.beans.PropertyDescriptor("minimum", getBeanClass());
            }
            /* aDescriptor.setBound(false); */
            /* aDescriptor.setConstrained(false); */
            aDescriptor.setDisplayName("minimumAvailableUnits");
            aDescriptor.setShortDescription("the minimum number of available units in the Stock");
            /* aDescriptor.setExpert(false); */
            /* aDescriptor.setHidden(false); */
            /* aDescriptor.setValue("preferred", new Boolean(false)); */
            /* aDescriptor.setValue("ivjDesignTimeProperty", new Boolean(true)); */
        } catch (Throwable exception) {
            handleException(exception);
        }
        return aDescriptor;
    }

    /**
     * Gets the producers property descriptor.
     *
     * @return java.beans.PropertyDescriptor
     */
    public java.beans.PropertyDescriptor producersPropertyDescriptor() {
        java.beans.PropertyDescriptor aDescriptor = null;
        try {
            try {
                /*
                 * Using methods via getMethod is the faster way to create the producers
                 * property descriptor.
                 */
                java.lang.reflect.Method aGetMethod = null;
                try {
                    /*
                     * Attempt to find the method using getMethod with parameter types.
                     */
                    Class<?> aGetMethodParameterTypes[] = {};
                    aGetMethod = getBeanClass().getMethod("getProducers", aGetMethodParameterTypes);
                } catch (Throwable exception) {
                    /* Since getMethod failed, call findMethod. */
                    handleException(exception);
                    aGetMethod = findMethod(getBeanClass(), "getProducers", 0);
                }
                java.lang.reflect.Method aSetMethod = null;
                aDescriptor = new java.beans.PropertyDescriptor("producers", aGetMethod, aSetMethod);
            } catch (Throwable exception) {
                /*
                 * Since we failed using methods, try creating a default property descriptor.
                 */
                handleException(exception);
                aDescriptor = new java.beans.PropertyDescriptor("producers", getBeanClass());
            }
            /* aDescriptor.setBound(false); */
            /* aDescriptor.setConstrained(false); */
            /* aDescriptor.setDisplayName("producers"); */
            aDescriptor.setShortDescription("The number of producers having stored products in this stock");
            /* aDescriptor.setExpert(false); */
            /* aDescriptor.setHidden(false); */
            /* aDescriptor.setValue("preferred", new Boolean(false)); */
            /* aDescriptor.setValue("ivjDesignTimeProperty", new Boolean(true)); */
        } catch (Throwable exception) {
            handleException(exception);
        }
        return aDescriptor;
    }

    /**
     * Gets the refused property descriptor.
     *
     * @return java.beans.PropertyDescriptor
     */
    public java.beans.PropertyDescriptor refusedPropertyDescriptor() {
        java.beans.PropertyDescriptor aDescriptor = null;
        try {
            try {
                /*
                 * Using methods via getMethod is the faster way to create the refused property
                 * descriptor.
                 */
                java.lang.reflect.Method aGetMethod = null;
                try {
                    /*
                     * Attempt to find the method using getMethod with parameter types.
                     */
                    Class<?> aGetMethodParameterTypes[] = {};
                    aGetMethod = getBeanClass().getMethod("getRefused", aGetMethodParameterTypes);
                } catch (Throwable exception) {
                    /* Since getMethod failed, call findMethod. */
                    handleException(exception);
                    aGetMethod = findMethod(getBeanClass(), "getRefused", 0);
                }
                java.lang.reflect.Method aSetMethod = null;
                aDescriptor = new java.beans.PropertyDescriptor("refused", aGetMethod, aSetMethod);
            } catch (Throwable exception) {
                /*
                 * Since we failed using methods, try creating a default property descriptor.
                 */
                handleException(exception);
                aDescriptor = new java.beans.PropertyDescriptor("refused", getBeanClass());
            }
            /* aDescriptor.setBound(false); */
            /* aDescriptor.setConstrained(false); */
            /* aDescriptor.setDisplayName("refused"); */
            aDescriptor.setShortDescription("The number of SimProcesses refused to enqueue because queue is full");
            /* aDescriptor.setExpert(false); */
            /* aDescriptor.setHidden(false); */
            /* aDescriptor.setValue("preferred", new Boolean(false)); */
            /* aDescriptor.setValue("ivjDesignTimeProperty", new Boolean(true)); */
        } catch (Throwable exception) {
            handleException(exception);
        }
        return aDescriptor;
    }

    /**
     * Gets the reset() method descriptor.
     *
     * @return java.beans.MethodDescriptor
     */
    public java.beans.MethodDescriptor resetMethodDescriptor() {
        java.beans.MethodDescriptor aDescriptor = null;
        try {
            /* Create and return the reset() method descriptor. */
            java.lang.reflect.Method aMethod = null;
            try {
                /*
                 * Attempt to find the method using getMethod with parameter types.
                 */
                Class<?> aParameterTypes[] = {};
                aMethod = getBeanClass().getMethod("reset", aParameterTypes);
            } catch (Throwable exception) {
                /* Since getMethod failed, call findMethod. */
                handleException(exception);
                aMethod = findMethod(getBeanClass(), "reset", 0);
            }
            try {
                /*
                 * Try creating the method descriptor with parameter descriptors.
                 */
                java.beans.ParameterDescriptor aParameterDescriptors[] = {};
                aDescriptor = new java.beans.MethodDescriptor(aMethod, aParameterDescriptors);
            } catch (Throwable exception) {
                /*
                 * Try creating the method descriptor without parameter descriptors.
                 */
                handleException(exception);
                aDescriptor = new java.beans.MethodDescriptor(aMethod);
            }
            /* aDescriptor.setDisplayName("reset()"); */
            aDescriptor.setShortDescription("resets the statistic sof this Stock");
            /* aDescriptor.setExpert(false); */
            /* aDescriptor.setHidden(false); */
            /* aDescriptor.setValue("preferred", new Boolean(false)); */
        } catch (Throwable exception) {
            handleException(exception);
        }
        return aDescriptor;
    }

    /**
     * Gets the retrieve(long) method descriptor.
     *
     * @return java.beans.MethodDescriptor
     */
    public java.beans.MethodDescriptor retrieve_longMethodDescriptor() {
        java.beans.MethodDescriptor aDescriptor = null;
        try {
            /* Create and return the retrieve(long) method descriptor. */
            java.lang.reflect.Method aMethod = null;
            try {
                /*
                 * Attempt to find the method using getMethod with parameter types.
                 */
                Class<?> aParameterTypes[] = { long.class };
                aMethod = getBeanClass().getMethod("retrieve", aParameterTypes);
            } catch (Throwable exception) {
                /* Since getMethod failed, call findMethod. */
                handleException(exception);
                aMethod = findMethod(getBeanClass(), "retrieve", 1);
            }
            try {
                /*
                 * Try creating the method descriptor with parameter descriptors.
                 */
                java.beans.ParameterDescriptor aParameterDescriptor1 = new java.beans.ParameterDescriptor();
                aParameterDescriptor1.setName("arg1");
                aParameterDescriptor1.setDisplayName("n");
                java.beans.ParameterDescriptor aParameterDescriptors[] = { aParameterDescriptor1 };
                aDescriptor = new java.beans.MethodDescriptor(aMethod, aParameterDescriptors);
            } catch (Throwable exception) {
                /*
                 * Try creating the method descriptor without parameter descriptors.
                 */
                handleException(exception);
                aDescriptor = new java.beans.MethodDescriptor(aMethod);
            }
            /* aDescriptor.setDisplayName("retrieve(long)"); */
            aDescriptor.setShortDescription("make the Stock retrieve n units for the consumer");
            /* aDescriptor.setExpert(false); */
            /* aDescriptor.setHidden(false); */
            /* aDescriptor.setValue("preferred", new Boolean(false)); */
        } catch (Throwable exception) {
            handleException(exception);
        }
        return aDescriptor;
    }

    /**
     * Gets the store(long) method descriptor.
     *
     * @return java.beans.MethodDescriptor
     */
    public java.beans.MethodDescriptor store_longMethodDescriptor() {
        java.beans.MethodDescriptor aDescriptor = null;
        try {
            /* Create and return the store(long) method descriptor. */
            java.lang.reflect.Method aMethod = null;
            try {
                /*
                 * Attempt to find the method using getMethod with parameter types.
                 */
                Class<?> aParameterTypes[] = { long.class };
                aMethod = getBeanClass().getMethod("store", aParameterTypes);
            } catch (Throwable exception) {
                /* Since getMethod failed, call findMethod. */
                handleException(exception);
                aMethod = findMethod(getBeanClass(), "store", 1);
            }
            try {
                /*
                 * Try creating the method descriptor with parameter descriptors.
                 */
                java.beans.ParameterDescriptor aParameterDescriptor1 = new java.beans.ParameterDescriptor();
                aParameterDescriptor1.setName("arg1");
                aParameterDescriptor1.setDisplayName("n");
                java.beans.ParameterDescriptor aParameterDescriptors[] = { aParameterDescriptor1 };
                aDescriptor = new java.beans.MethodDescriptor(aMethod, aParameterDescriptors);
            } catch (Throwable exception) {
                /*
                 * Try creating the method descriptor without parameter descriptors.
                 */
                handleException(exception);
                aDescriptor = new java.beans.MethodDescriptor(aMethod);
            }
            /* aDescriptor.setDisplayName("store(long)"); */
            aDescriptor.setShortDescription("Method used by producers to store a number of units in the stock");
            /* aDescriptor.setExpert(false); */
            /* aDescriptor.setHidden(false); */
            /* aDescriptor.setValue("preferred", new Boolean(false)); */
        } catch (Throwable exception) {
            handleException(exception);
        }
        return aDescriptor;
    }

    /**
     * Called whenever the bean information class throws an exception.
     *
     * @param exception java.lang.Throwable
     */
    private void handleException(java.lang.Throwable exception) {

        /* Uncomment the following lines to print uncaught exceptions to stdout */
        // System.out.println("--------- UNCAUGHT EXCEPTION ---------");
        // exception.printStackTrace(System.out);
    }
}
