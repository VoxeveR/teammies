import { NavLink } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faBars } from '@fortawesome/free-solid-svg-icons';
import React from 'react';
import { useAuth } from '../../hooks/useAuth'; // <-- import hook
import { useNavigate } from 'react-router-dom';

type NavItem = {
      label: string;
      linkTo?: string;
      onClick?: () => void;
};

type NavbarProps = {
      elements?: Array<NavItem>; // optional extra items
      showMobile?: boolean;
};

function Navbar({ elements = [], showMobile = true }: NavbarProps) {
      const { isAuthenticated, logout } = useAuth();
      const navigate = useNavigate();

      const authItems: NavItem[] = isAuthenticated
            ? [
                    {
                          label: 'Leagues',
                          linkTo: '/leagues',
                    },
                    {
                          label: 'Logout',
                          onClick: async () => {
                                await logout();
                                navigate('/login');
                          },
                    },
              ]
            : [
                    { label: 'Login', linkTo: '/login' },
                    { label: 'Register', linkTo: '/register' },
              ];

      return (
            <div className='bg-quiz-white flex h-fit w-full flex-col items-center pb-4 first:pt-4 last:pb-4 lg:h-24 lg:flex-row lg:ps-4'>
                  <NavLink className='flex w-fit flex-col items-center gap-2 lg:max-w-[500px] lg:flex-row lg:gap-2' to='/'>
                        <img src='/src/assets/logo.svg' className='h-32 w-32 shrink-0 lg:h-20 lg:w-20' />
                        <div className='truncate text-center font-[Bungee] text-3xl lg:text-left lg:text-5xl'>TEAMMIES</div>
                  </NavLink>

                  <div className='flex w-full flex-col'>
                        {showMobile && (
                              <label htmlFor='toggle' className='flex cursor-pointer items-center justify-center lg:hidden'>
                                    <FontAwesomeIcon icon={faBars} className='text-3xl' />
                              </label>
                        )}

                        <input type='checkbox' id='toggle' className='peer hidden' />

                        <div className='hidden w-full flex-col items-center gap-1 pt-4 text-2xl peer-checked:flex lg:flex lg:flex-row lg:justify-end lg:gap-4 lg:pe-4 lg:pt-0 lg:text-3xl'>
                              {[...elements, ...authItems].map((e) =>
                                    e.onClick ? (
                                          <button
                                                key={e.label}
                                                onClick={e.onClick} // tutaj wywołujemy logout
                                                className='hover:text-quiz-light-green cursor-pointer'
                                          >
                                                {e.label}
                                          </button>
                                    ) : (
                                          <NavLink key={e.linkTo} to={e.linkTo!} className='hover:text-quiz-light-green'>
                                                {e.label}
                                          </NavLink>
                                    )
                              )}
                        </div>
                  </div>
            </div>
      );
}

export default React.memo(Navbar);
