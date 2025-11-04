import { NavLink } from 'react-router-dom';

type NavItem = {
      label: string;
      linkTo: string;
};

type NavbarProps = {
      elements?: Array<NavItem>;
};

export default function Navbar({ elements }: NavbarProps) {
      return (
            <div className='bg-quiz-white flex h-24 flex-row items-center gap-4 ps-4'>
                  <NavLink className='flex items-center' to='/'>
                        <img src='/src/assets/logo.svg' className='h-20 w-20'></img>
                        <div className='text-7xl'>TEAMMIES</div>
                  </NavLink>
                  <div className='flex grow flex-row justify-end gap-4 pe-4 text-3xl'>
                        {elements?.map((element) => (
                              <div>{element.label}</div>
                        ))}
                  </div>
            </div>
      );
}
