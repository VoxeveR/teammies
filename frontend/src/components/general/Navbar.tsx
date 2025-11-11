import { NavLink } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faBars } from '@fortawesome/free-solid-svg-icons';

type NavItem = {
      label: string;
      linkTo: string;
};

type NavbarProps = {
      elements?: Array<NavItem>;
      showElements?: boolean;
      showMobile?: boolean;
};

export default function Navbar({ elements, showMobile = true }: NavbarProps) {
      return (
            <div className='bg-quiz-white flex h-fit w-full flex-col items-center ps-4 pb-4 first:pt-4 last:pb-4 lg:h-24 lg:flex-row'>
                  <NavLink className='flex items-center' to='/'>
                        <img src='/src/assets/logo.svg' className='h-32 w-32 lg:h-20 lg:w-20' />
                  </NavLink>
                  <div className='text-3xl lg:ml-2 lg:text-5xl'>TEAMMIES</div>
                  <div className='flex w-full flex-col'>
                        {showMobile ? (
                              <label htmlFor='toggle' className='flex cursor-pointer items-center justify-center lg:hidden'>
                                    <FontAwesomeIcon icon={faBars} className='text-3xl' />
                              </label>
                        ) : (
                              ''
                        )}

                        <input type='checkbox' id='toggle' className='peer hidden' />

                        <div
                              id='control-me'
                              className='hidden w-full cursor-pointer flex-col items-center gap-1 pt-4 text-2xl peer-checked:flex lg:flex lg:flex-row lg:justify-end lg:gap-4 lg:pe-4 lg:pt-0 lg:text-3xl'
                        >
                              {elements?.map((element) => (
                                    <NavLink key={element.linkTo} to={element.linkTo} className='hover:text-quiz-light-green'>
                                          {element.label}
                                    </NavLink>
                              ))}
                        </div>
                  </div>
            </div>
      );
}
