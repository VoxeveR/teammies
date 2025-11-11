import Navbar from '../components/general/Navbar.tsx';

const inputStyle = "w-full p-2 border bg-quiz-green text-quiz-white rounded-md mb-2 rounded-xl";
const buttonStyle = "xd";

function LoginPage() {
        return (
            <div className="w-full h-full max-h-screen overflow-hidden flex flex-col">
                <Navbar></Navbar>
                <div className="w-full h-full bg-quiz-white flex flex-col pt-15 lg:bg-none lg:justify-center  lg:w-1/2 lg:mx-auto lg:h-2/3 lg:rounded-xl lg:my-auto">
                    <div className="w-full flex flex-col justify-center items-center gap-1">
                        <div className="text-[40px]">Welcome back!</div>
                        <div className="text-[16px] text-quiz-light-green pb-4">Please, enter your details to sign in.</div>
                    </div>
                    <form className="flex flex-col w-4/5 mx-auto mt-2 gap-2">
                        <label className="text-[16px] text-quiz-green">Email</label>
                        <input type="text" placeholder="Enter your email" className="w-full p-2 bg-quiz-green text-quiz-white mb-2 rounded-xl"></input>
                        <label className="text-[16px] text-quiz-green">Password</label>
                        <input type="password" placeholder="Enter your password" className={inputStyle}></input>
                        <div className="flex justify-center items-center ">Forgot your password?</div>
                        <button type="submit" className="w-full bg-quiz-dark-green text-quiz-white p-3 rounded-xl">LOGIN</button>
                    </form>
                    <div>
                        <div className="text-center mt-4">Don't have an account? <a href="/register" className="text-quiz-light-green underline">Register here</a></div>
                    </div>
                </div>
            </div>
        )
}   

export default LoginPage;