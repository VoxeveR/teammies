import { isRouteErrorResponse, useRouteError } from 'react-router-dom';
import ErrorDisplay from '../components/general/ErrorDisplay';

type Props = {
      error?: unknown;
};

export default function ErrorPage({ error: errorProp }: Props) {
      const routeError = errorProp ?? useRouteError();

      if (isRouteErrorResponse(routeError)) {
            return <ErrorDisplay title={`${routeError.status} - ${routeError.statusText.toUpperCase()}`} message={String(routeError.data)} />;
      } else if (routeError instanceof Error) {
            return <ErrorDisplay title='Error' message='Something went wrong. Please try again later.' stack={routeError.stack} isFullHeight />;
      } else {
            return <ErrorDisplay title='Unknown Error' message='Something went wrong. Please try again later.' isFullHeight />;
      }
}
