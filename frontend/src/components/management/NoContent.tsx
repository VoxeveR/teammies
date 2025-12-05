import type { ReactNode } from 'react';
import Logo from '../../assets/logo.svg';

interface NoContentProps {
      title?: string;
      description?: string;
      imageSrc?: string;
      children?: ReactNode;
}

export default function NoContent({ title, description, imageSrc = Logo, children }: NoContentProps) {
      return (
            <div className='bg-quiz-white flex h-fit flex-col items-center justify-center rounded-2xl border p-8'>
                  <img src={imageSrc} alt={title} className='h-32 w-32' />
                  <div className='text-quiz-dark-green text-center'>
                        <div className='text-3xl font-semibold'>{title}</div>
                        <div className='text-quiz-dark-green mb-2 text-xl'>{description}</div>
                  </div>
                  {children && <div className='mt-0'>{children}</div>}
            </div>
      );
}
