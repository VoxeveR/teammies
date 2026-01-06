import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

const AuthenticatedRoute = () => {
      const auth = useAuth();

      if (!auth.isAuthenticated) {
            return <Navigate to='/login' replace></Navigate>;
      }

      return <Outlet></Outlet>;
};

export default AuthenticatedRoute;
