import type { IconDefinition } from '@fortawesome/fontawesome-svg-core';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

interface FeatureCardProps {
      icon: IconDefinition;
      title: string;
      description: string;
}

function FeatureCard({ icon, title, description }: FeatureCardProps) {
      return (
            <div className='border-quiz-dark-green flex flex-col gap-2 rounded-2xl border p-6 lg:h-full lg:w-full'>
                  <div className='flex flex-row gap-4'>
                        <FontAwesomeIcon icon={icon} className='text-3xl' />
                        <div className='text-quiz-dark-green text-2xl'>{title}</div>
                  </div>
                  <hr className='text-quiz-dark-green'></hr>
                  <div className='text-quiz-light-green text-lg'>{description}</div>
            </div>
      );
}

export default FeatureCard;
