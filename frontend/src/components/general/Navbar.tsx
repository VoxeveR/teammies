import { NavLink } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faBars } from '@fortawesome/free-solid-svg-icons';

type NavItem = {
      label: string;
      linkTo: string;
};

type NavbarProps = {
      elements?: Array<NavItem>;
};

export default function Navbar({ elements }: NavbarProps) {
      return (
      <div className='bg-quiz-white flex flex-col lg:flex-row items-center w-full h-fit lg:h-24 first:pt-4 last:pb-4 ps-4'>
            <NavLink className='flex items-center' to='/'>
                  <img src='/src/assets/logo.svg' className='h-32 w-32 lg:h-20 lg:w-20' />
            </NavLink>
            <div className='text-3xl'>TEAMMIES</div>

            <div className="flex flex-col w-full">

            <label htmlFor="toggle" className="flex items-center justify-center lg:hidden cursor-pointer">
                  <FontAwesomeIcon icon={faBars} className="text-3xl" />
            </label>

            <input type="checkbox" id="toggle" className="hidden peer" />

                  <div
                        id="control-me"
                        className='hidden peer-checked:flex flex-col w-full lg:flex lg:flex-row lg:justify-end items-center gap-1 pt-4 lg:pt-0 lg:gap-4 lg:pe-4 text-2xl lg:text-3xl cursor-pointer'
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
