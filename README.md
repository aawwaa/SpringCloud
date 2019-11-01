# SpringCloud
搭建以eureka为注册中心，config为配置中心得springcloud架构

springboot：2.2.0.RELEASE
springcloud：Hoxton.M3
jdk：1.8
git仓库：本人git创建的私有仓库,名为config


                                               搭建流程

1.模块关系：聚合与继承
	1.概念
		聚合：是为了更快的构建项目，当某个项目构建，其聚合得所有模块都会依次构建
	
		继承：继承是为了消除重复；继承的特性是指建立一个父模块，我们项目中的多个模块都做为该模块的子模块，将各个子模块相同的依赖和插件配置提取出来，从而简化配置文件，父模块的打包方式必须为pom，否则无法构建项目
	
	2.关系：
		聚合情况下，聚合模块知道它聚合的模块，但被聚合模块不知道聚合模块的存在。

		继承情况下，父模块不知道子模块的存在，但子模块都知道父模块是谁。
		
		通常情况下，聚合与继承是同时使用的使用聚合来实现各个模块的聚拢和同意构建；通过继承来实现子模块对父模块jar包的引用。
		
	3.使用场景：
		创建springcloud架构：
			1.在创建一个maven总工程的时候，架构中的所有项目都聚合在总工程下面，且打包模式是pom模式
				
				总工程聚合代码：
					<modules>
					   <module>模块一</module>
					   <module>模块二</module>
					   <module>模块三</module>
					</modules>
					
					
			2.创建一个maven公共模块，聚合在总工程下面；
				配置文件中继承总工程模块；
				总工程引入(module)该模块；
				公共模块主要是管理一些公共的配置，共用的bean之类的。
			
			3.创建功能模块，聚合在总工程下面；
				配置文件继承总工程模块；
				配置文件引入公共模块；
				总工程引入(module)该模块；
	
	
2.项目架构：
	
	--总工程：maven模块，pom打包方式，继承 <artifactId>spring-boot-starter-parent</artifactId>
		
		--common模块：公共配置，公用bean等，公共服务。
				
		--eureka模块(集群)：服务注册中心。用于发现服务，用于服务注册与发现
		
		--config模块(集群)：服务配置中心。用于集成各个模块的配置文件，做到统一配置，易于管理。
		
		--producer模块：服务生产者，上游服务，用于服务生产
		
		--consumer模块：服务消费者，下游服务，用于服务消费
		
		--gateway(zuul)模块：网关，微服务与外界通信的唯一出入口，起到限流，过滤和校验的作用。所有请求必须通过网关的校验才可以访问微服务组件
		
		
	--组件：作用于服务模块(生产者，消费者模块的微服务组件)
		
		--ribbon：负载均衡组件，用于服务集群下单一服务的请求合理分配
		
		--Hystrix：熔断器组件，用于服务降级与熔断保护
		
		--feign：feign底层是使用了ribbon作为负载均衡的声明式的web service客户端，用于系统间服务的调用
		
		
3.搭建项目(eureka:注册中心；config:注册中心)
	1.创建总工程，一个maven项目
		
		a.pom文件继承springcloud依赖
		
		b.pom文件添加大多应用使用到的jar包
			例如：数据库相关依赖，lombok包，日志包，fastjson包，test包，common包
			
		c.pom文件添加聚合的模块
		
	2.创建公共依赖模块，一个maven项目
		
		a.一些公用的bean,utils等可以提取到公共模块
		
		
	3.创建服务注册中心(集群): 配置中心地址访问(localhost:8761,8762)
		
		1.创建springboot工程，配置为注册中心
		
		2.pom文件继承总工程为父类
		
			添加依赖：
				<!--eureka-server服务端依赖 -->
				<dependency>
					<groupId>org.springframework.cloud</groupId>
					<artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
				</dependency>
		
		3.可以配置集群下本地映射
			1.配置两个eureka(接口：8761,8762) 
			
			2.host文件配置：
				127.0.0.1	eureka1.com
				127.0.0.1	eureka2.com	
				
			3.原因：hostname和defaultZone的域名不要设置为localhost，应设置为别名，并在操作系统的host文件中添加映射，否则后台会出现注册为空的情况
			
		4.修改yml文件：
			
			其中一个eureka配置：
				eureka:
					instance:
						hostname: localhost                 											#服务注册中心实例的主机名
					client:
						register-with-eureka: false         											#是否在eureka服务器上注册自己的信息以供其他服务发现，默认为true  是否注册自己
						fetch-registry: true                											#此客户端是否获取eureka服务器注册表上的注册信息，默认为true      如果是单机就设置为false
						serviceUrl:
							#defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/     #与Eureka注册服务中心的通信zone和url地址  		单机模式
							 defaultZone: http://eureka1.com:8761/eureka/  								#与Eureka注册服务中心的通信zone和url地址		集群模式

			另一个eureka配置相似，仅需修改defaultZone属性配置，为除自身外其他的注册中心:
				defaultZone: http://eureka2.com:8762/eureka/  											#与Eureka注册服务中心的通信zone和url地址		集群模式

		5.启动类激活服务发现
			添加注解：@EnableEurekaServer


	4.创建配置中心服务(以github为仓库)
		
		1.在git仓库上创建config-application文件夹且创建一个config-info-dev.yml配置文件
		
			a.config-info-dev.yml 格式固定为：application-profile
				application：配置文件名，config-info
				profile:	 生产环境名，dev	
				
			b.文件内容(测试使用)
				cn:
				  springcloud:
					boot:
					  config: this is a dev
				
		2.创建一个sprongboot工程作为配置中心
		
		3.pom文件添加依赖
			
			a.由于需要注册到注册中心，需要eureka注册服务端依赖：
				<!-- 将微服务microservice-provider侧注册进eureka -->
				<dependency>
					<groupId>org.springframework.cloud</groupId>
					<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
					<version>2.1.3.RELEASE</version>
				</dependency>	
				
			b.添加配置中心的服务端依赖
				<!--配置中心的坐标-->
				<dependency>
					<groupId>org.springframework.cloud</groupId>
					<artifactId>spring-cloud-config-server</artifactId>
				</dependency>
		
		4.修改yml文件
		
			配置eureka客户端：配置指向git仓库的相关配置
				spring:
				  cloud:
					config:
					  server:
						git:
						  uri: https://github.com/aawwaa/config.git 	#git仓库地址
						  search-paths: config-application,consumer		#配置文件搜索范围，可配置多个分类文件夹
						  skip-ssl-validation: true						#跳过验证ssl
						  username: 123456789@qq.com					#账号
						  password: mima								#git密码
				  application:
					name: config-dev						

				eureka:
				  client:
					service-url:
					  defaultZone: http://eureka1:8761/eureka/,http://eureka2:8762/eureka/

				server:
				  port: 8090
				  
		5.启动类激活注册中心，和添加注册中心客户端
			@EnableConfigServer、@EnableEurekaClient  
	
		6.可以验证一下，是否配置中心可以拿到配置：
			
			请求格式：http//注册中心端口/application/profile/branch
			
			直接请求：http//localhost:8090/config-info/dev/master 
			若可以拿到git仓库config-info-dev.yml内容，说明配置成功
			
	
	5.创建生产者(消费者)
		1.创建springboot工程
		
		2.pom文件继承总工程为父类;
			添加公共模块依赖；
			
				dependency>
					<groupId>com.tarot</groupId>
					<artifactId>common</artifactId>
					 <version>{project.version}</version>
				</dependency>
			
			添加eureka客户端依赖，注册中心客户端，web依赖：若没有web，则注册eureka项目启动会失败
				<dependency>
					<groupId>org.springframework.boot</groupId>
					<artifactId>spring-boot-starter-web</artifactId>
				</dependency>

				<!-- eureka客户端 -->
				<dependency>
					<groupId>org.springframework.cloud</groupId>
					<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
					<version>2.1.3.RELEASE</version>
				</dependency>
				
				<!--注册中心客户端-->
				<dependency>
					<groupId>org.springframework.cloud</groupId>
					<artifactId>spring-cloud-starter-config</artifactId>
				</dependency>
		
		3.yml文件配置
			1.由于配置文件要统一管理，所以只配置配置中心相关信息，其他配置统一集中存放在git上统一管理，以供该服务使用
			
			2.配置文件名application.yml 修改为bootstrap.yml，注意该文件名是bootstrap。
				
				因为启动SpringBoot项目时，会优先读取bootstrap.yml里的配置，再读取远程配置中心的配置，最后才会读取application.yml。如果不通过bootstrap.yml里的配置指向，
					找不到配置中心就会读取默认的配置中心http//localhost:8888/,永远无法找到正确配置。
			
			3.配置内容
				spring:
				  cloud:
					config:
					  uri: http://localhost:8090/		  # 配置中心地址	
					  name: producer                      # application文件名
					  profile: dev                        # 指定profile的环境
					  label: master                       # 指定读取分支
					  discovery:
						enabled: true
						service-id: config-dev            # 注册中心的服务名
					  
		4.远程配置(配置中心管理的git仓库中)
			a.和项目yml文件配置的名字一致
				在git仓库创建一个：producer-dev.yml配置文件

			
			b.配置内容(所有用到的配置)
				server:
				  port: 8004

				spring:
				  application:
					name: consumer

				eureka:
				  client:
					service-url:
					  defaultZone: http://eureka1:8761/eureka/,http://eureka2:8762/eureka/
					  
									
		5.启动类激活服务发现的客户端	
			添加注解：@EnableEurekaClient
			
			
	
		



