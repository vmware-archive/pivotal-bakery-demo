# Pivotal Bakery - Spring Cloud Services and PivotalSSO Demo

## Overview
Pivotal Bakery is a 're-skin' of Will Tran's Freddy BBQ Demo

This demo is an example of using Pivotal SSO and Spring Cloud Services in a microservice architecture.


This is the use case:
- Give Customer the ability to see the menu online and place orders
- Give Owner the ability to manage the menu and close orders


## Building the Demo

Note: using maven to package with "mvn package -DskipTests" as local test harness removed from project.
 

## Deploy to Cloud Foundry

Rename manifest.yml.template to manifest.yml - modify TRUST_CERTS if using self-signed certs for SCS, and CF push (ensuring sso, mysql, circuit-breaker, serivce-registry & config-server services are available)

1. Install dependent Services
2. Create service instances for microservices to bind to
3. Set SKIP_SSL_VALIDATION for all applications (NOTE: Only if TLS is self-signed cert)
4. Deploy applications to Cloud Foundry
5. Setup authorization scopes and users (customer & employee)
6. Add authorization scopes to microservices in SSO management dashboard
7. Verify application works

### Install Dependent Services

Pivotal Bakery requires the installation of the following Pivotal Cloud Foundry products:

* [SSO for PCF](https://network.pivotal.io/products/p-identity)
* [MySQL for PCF](https://network.pivotal.io/products/p-mysql)
* [Spring Cloud Services for PCF](https://network.pivotal.io/products/p-spring-cloud-services)



After installing these products and setting up an SSO provider, you can verify that they are available for creating service instances by running `cf marketplace` in a terminal. You should see the following services available in the PCF marketplace:

Create auth-domain (as PCF administrator), ensuring 'customer' users have "order.me and menur.read" scopes and 'employee/owner' users have scopes 'order.me, order.admin, menu.read, menu.write' scopes.

### Create Service instances

The following service instances must be created before pushing the application instances involved in running Pivotal Bakery:

* sso
* mysql
* service-registry
* circuit-breaker
* config-server

To create all of these service instances run the following commands:

```
cf create-service p-identity auth sso
cf create-service p-mysql 100mb mysql
cf create-service p-service-registry standard service-registry
cf create-service p-circuit-breaker-dashboard standard circuit-breaker
cf create-service p-config-server standard config-server
```

Some of these services are created asynchronously. In order to verify that they were created successfully you can run the following command and look for output that includes `create succeeded` on each line in the `last operation` column:

```
$ cf services
name              service                       plan       last operation
circuit-breaker   p-circuit-breaker-dashboard   standard   create succeeded
config-server     p-config-server               standard   create succeeded
mysql             p-mysql                       100mb      create succeeded
service-registry  p-service-registry            standard   create succeeded
sso               p-identity                    auth       create succeeded
```

### Set CF_TARGET

If your PCF environment is deployed with self-signed certificates to enable TLS for application domains then you must set TRUST_CERTS environment variable for deployed microservices to the API endpoint for PCF. Run the following command as a Cloud Foundry Administrator to set TRUST_CERTS to the API endpoint for all applications deployed into PCF environment:

```
cf srevg '{"TRUST_CERTS":"https://api.mypcf.example.com"}'
```
### Deploy Applications

The file `manifest.yml` contains the configuration for all of the application instances to be deployed for Pivotal Bakery. In order to deploy the applications to PCF run the following command from the folder containing `manifest.yml`:

```
cf push
```

### Setup Authorization Scopes and Users

There are 2 scripts in the `/uaa` directory that will create the necessary authorized scopes and users for the Pivotal Bakery applications:

```
/uaa
  - zoneadmin.sh
  - zoneusers.sh
```

These scripts need some environment variables defined in order to run against Cloud Foundry UAA endpoints.

```
# UAA_ENDPOINT eg uaa.mypcf.example.com
# ADMIN_CLIENT_ID eg admin
# ADMIN_CLIENT_SECRET get this from Ops Manager Elastic Runtime UAA "Admin Client Credentials"
# IDENTITY_ZONE_ID this is the GUID of the identity zone which is the first GUID in the URI for any page in the `sso` service instance dashboard
# ZONEADMIN_CLIENT_ID pick a name for the admin client in the zone
# ZONEADMIN_CLIENT_SECRET
# ZONE_ENDPOINT the auth domain URL from SSO for PCF eg auth.login.mypcf.example.com
```

After you set these environment variables, run the following commands:

```
./uaa/zoneadmin.sh
./uaa/zoneusers.sh
```

This should run through successfully creating 2 users, `employee` the administrator and `customer` the user. It should also create four authorization scopes that users and applications can be configued to access: `menu.read`, `menu.write`, `order.admin`, and `order.me`.

### Authorize Applications to Scopes

In order for the `admin` and `customer` portals of Pivotal Bakery application to access the dependent microservices `menu-service` and `order-service` you must configure the 'scopes' in the `sso` service dashboard. In order to access the `sso` dashboard, run the following command and go to the URL listed in `Dashboard` property:

```
$ cf service sso

Service instance: sso
Service: p-identity
Bound apps: customer-portal,menu-service,admin-portal,order-service
Tags:
Plan: auth
Description: Single Sign-On as a Service
Documentation url: http://docs.pivotal.io/p-identity/index.html
Dashboard: https://p-identity.mypcf.example.com/dashboard/identity-zones/{ZONE_GUID}/instances/{INSTANCE_GUID}/
...
```
On the dashboard, add all of the scopes for Pivotal Bakery application instances: `menu.read`, `menu.write`, `order.admin`, and `order.me`.

### Verify Application Works

Go to the Customer and Admin Portals via a web browser. Use different browser sessions for each so that you can authenticate with the appropriate user.

* For Customer Portal [http://customer-portal.mypcf.example.com] authenticate as `customer`
* For Admin Portal [http://admin-portal.mypcf.example.com] authenticate as `employee`

You should now be able to order food from the menu as `customer` and add items for sale to the menu as `employee`.
