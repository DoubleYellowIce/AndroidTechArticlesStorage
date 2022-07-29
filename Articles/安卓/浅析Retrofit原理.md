相信有过Android开发经验的小伙伴应该对Retrofit不陌生，只要涉及到Http通信，那Retrofit基本上就没跑了。

Retrofit想必大家也是用得贼6，不用我介绍，所以这篇文章我更多想浅谈一下我对Retrofit原理的理解。

- Retrofit的实例化。

```kotlin
private val retrofit=Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
```

可以看到，Retrofit的实例化用到了生成器设计模式。

baseUrl()源码如下所示。

```java
public Builder baseUrl(String baseUrl) {
      checkNotNull(baseUrl, "baseUrl == null");
      return baseUrl(HttpUrl.get(baseUrl));
}
```

addConverterFactory()源码如下所示。

```java
public Builder addConverterFactory(Converter.Factory factory) {
      converterFactories.add(checkNotNull(factory, "factory == null"));
      return this;
}
```

可以看到上述两个方法都是比较简单的。

```java
    public Retrofit build() {
      //检查baseUrl是否为空
      if (baseUrl == null) {
        throw new IllegalStateException("Base URL required.");
      }
			
      //1
      okhttp3.Call.Factory callFactory = this.callFactory;
      if (callFactory == null) {
        callFactory = new OkHttpClient();
      }

      //获取负责执行回调任务的线程池
      Executor callbackExecutor = this.callbackExecutor;
      if (callbackExecutor == null) {
        callbackExecutor = platform.defaultCallbackExecutor();
      }
			
      // Make a defensive copy of the adapters and add the default Call adapter.
      List<CallAdapter.Factory> callAdapterFactories = new ArrayList<>(this.callAdapterFactories);
      callAdapterFactories.addAll(platform.defaultCallAdapterFactories(callbackExecutor));

      // Make a defensive copy of the converters.
      List<Converter.Factory> converterFactories = new ArrayList<>(
          1 + this.converterFactories.size() + platform.defaultConverterFactoriesSize());

      // Add the built-in converter factory first. This prevents overriding its behavior but also
      // ensures correct behavior when using converters that consume all types.
      converterFactories.add(new BuiltInConverters());
      converterFactories.addAll(this.converterFactories);
      converterFactories.addAll(platform.defaultConverterFactories());

      return new Retrofit(callFactory, baseUrl, unmodifiableList(converterFactories),
          unmodifiableList(callAdapterFactories), callbackExecutor, validateEagerly);
    }
```

从1处代码可以知道Retrofit是对OkHttpClient库进行了封装。

- create()方法。

  不知道小伙伴在使用Retrofit有没有过这样的疑惑，为什么Service是一个接口，它的方法却可以直接调用？

  要回答这个问题，还是要回到Retrofit的create()源码中。

  ```java
  public <T> T create(final Class<T> service) {
      Utils.validateServiceInterface(service);
      if (validateEagerly) {
        eagerlyValidateMethods(service);
      }
      return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[] { service },
          new InvocationHandler() {
            private final Platform platform = Platform.get();
            private final Object[] emptyArgs = new Object[0];
  
            @Override public @Nullable Object invoke(Object proxy, Method method,
                @Nullable Object[] args) throws Throwable {
              // If the method is a method from Object then defer to normal invocation.
              //1
              if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
              }
              //2
              if (platform.isDefaultMethod(method)) {
                return platform.invokeDefaultMethod(method, service, proxy, args);
              }
              //3
              return loadServiceMethod(method).invoke(args != null ? args : emptyArgs);
            }
          });
    }
  ```

  噢，原来是动态代理嘛。

  当我们调用Service的方法的时候，实际上就会调用到匿名类InvocationHandler的invoke()方法里，代码1处和代码2处判断调用的方法是不是Service里面的。

  - 如果是。则调用loadServiceMethod()，在这个方法里会通过反射获取方法参数还有方法注解。
  - 如果不是。则调用Object里或平台的默认方法。

#### 参考资料

1. 《Android进阶之光》，刘望舒
2. [How does Retrofit work](https://proandroiddev.com/how-does-retrofit-work-6ecad1bb683b)
3. [Understand How does Retrofit work](https://medium.com/mindorks/understand-how-does-retrofit-work-c9e264131f4a)