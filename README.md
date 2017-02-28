Spring Web Authorisation framework based on http://www9.org/w9-papers/EC-Security/153.pdf.

Example RestController :

```
@RestController
public class UsersController {
    @Permission(permission = {ViewAllUsersPermission.class})
    @RequestMapping(value="/users", produces={"application/json"}, method = {RequestMethod.GET})
    public List<User> home() {
        logger.info("Got call for users");
        return userService.getAllUsers();
    }

    @Permission(permission = {CreateUserPermission.class, CreateAdminPermission.class})
    @RequestMapping(value="/users", produces={"application/json"}, method = {RequestMethod.POST})
    public User addUser(@RequestBody @Valid User user) throws UserErrorException {
        try {
            return userService.saveUser(user);
        } catch (UserAlreadyExistsException ex) {
            logger.info("User already exists.");
            throw new UserErrorException(userAlreadyExistsMessage, HttpStatus.BAD_REQUEST);
        }
    }

    @Permission(permission = {ViewUserPermission.class})
    @RequestMapping(value="/users/{userId}", produces={"application/json"}, method = {RequestMethod.GET})
    public User getUser(@PathVariable("userId") String userId) throws UserErrorException {
        User user = userService.getUserById(userId);
        if(user == null) {
            throw new UserErrorException(userDoesNotExists, HttpStatus.BAD_REQUEST);
        }
        return user;
    }
}
```
Example permission class:
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

Authenticated users can have multiple roles and roles can be fetched via concrete implementation of RoleService.
For example, RoleService can fetch roles from Mongo :
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
