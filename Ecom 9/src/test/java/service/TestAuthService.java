package service;

import com.TDD.Ecom.repo.UserRepo;
import com.TDD.Ecom.service.AuthService;

public class TestAuthService extends AuthService {

    private boolean isAdmin = true; // default to true

    public TestAuthService(UserRepo userRepo) {
        super(userRepo, null, null);
    }

    public void setAdminUser(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    @Override
    public boolean isAdmin(String email) {
        return isAdmin;
    }
}
