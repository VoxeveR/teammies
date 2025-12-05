export default {
      content: ['./index.html', './src/**/*.{js,jsx,ts,tsx}', './node_modules/flowbite-react/lib/**/*.js', './node_modules/react-tailwindcss-datepicker/dist/index.esm.{js,ts}'],
      theme: {
            extend: {},
      },
      plugins: [require('flowbite/lib/plugin')],
};
