Spring Authorisation framework based on http://www9.org/w9-papers/EC-Security/153.pdf.


How to use?

This framework works in conjunction with spring-security.
`SecurityContextHolder.getContext().getAuthentication().getPrincipal()` should return authenticated user
class object (this can depend on your project) and `SecurityContextHolder.getContext().getAuthentication().getAuthorities()`
should return `List<? extends GrantedAuthority>` of authenticated user. `RoleService.getRoles().get(GrantedAuthority.getAuthority())` will be used to
get actual `Role` object. How you set these values, is up to you. A sample implementation of RoleService is provided
in `src/main/impl/ConcreteRoleService` which fetches roles from mongodb.
```
{
	"_id" : ObjectId("58a435cbc5e5637d317d2d81"),
	"name" : "User",
	"permissions" : [
		"ViewUserPermission"
	]
}
{
	"_id" : ObjectId("58a582bc85078ec046b7fa06"),
	"name" : "AnonymousUser",
	"permissions" : [
		"CreateUserPermission"
	]
}
```

Next, create a permission class like below :
```
@Component("ViewAllUsersPermission")
public class ViewAllUsersPermission extends BasePermission {

  @Override
  public boolean isAuthorised(Object authenticatedUser, RequestObject requestObject)
      throws AuthorisationException {
    AuthenticatedUser user = (AuthenticatedUser) authenticatedUser;
    if(user.isAdmin()) {
      return true;
    }

    return false;
  }

  @Override
  public boolean useReturnValueAsBusinessObject() {
    return true;
  }
}
```

Next annotate `RestController` method, which requires this permission, like below:

```
@RestController
public class UsersController {
    @Permission(permission = {ViewAllUsersPermission.class})
    @RequestMapping(value="/users", produces={"application/json"}, method = {RequestMethod.GET})
    public List<User> home() {
        logger.info("Got call for users");
        return userService.getAllUsers();
    }
}
```
This should be it.
Now, when a authenticated user logs in, she/he will only be able to access this method if it has required
permission and permission evaluates to true.

Multiple permissions on Method:
As, you may have noticed, `permission` in `@Permission(permission = {ViewAllUsersPermission.class})`
is an array. So, multiple permissions can be entered. User having either one of them will be allowed
to proceed. It is like ORing the permissions. Support for ANDing is not there (can be added later).

Multiple Roles:
User can have multiple roles.