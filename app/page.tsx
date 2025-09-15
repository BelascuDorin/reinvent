import { Button } from "@/components/ui/button"
import { Card, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Users, Calendar, MessageCircle, Star } from "lucide-react"
import Link from "next/link"

export default function HomePage() {
  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header className="border-b border-border bg-card">
        <div className="container mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center">
              <Users className="w-5 h-5 text-primary-foreground" />
            </div>
            <h1 className="text-xl font-semibold text-foreground">MentorConnect</h1>
          </div>
          <nav className="flex items-center space-x-4">
            <Link href="/auth/signin">
              <Button variant="outline">Sign In</Button>
            </Link>
            <Link href="/auth/signup">
              <Button>Get Started</Button>
            </Link>
          </nav>
        </div>
      </header>

      {/* Hero Section */}
      <section className="py-20 px-4">
        <div className="container mx-auto text-center max-w-4xl">
          <h2 className="text-4xl md:text-6xl font-bold text-foreground mb-6 text-balance">
            Connect with Romanian Professionals
          </h2>
          <p className="text-xl text-muted-foreground mb-8 text-pretty">
            Discover your career path by learning directly from experienced professionals. Get personalized advice,
            insights, and guidance for your future.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link href="/auth/signup?role=mentee">
              <Button size="lg" className="w-full sm:w-auto">
                Find a Mentor
              </Button>
            </Link>
            <Link href="/auth/signup?role=mentor">
              <Button variant="outline" size="lg" className="w-full sm:w-auto bg-transparent">
                Become a Mentor
              </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-16 px-4 bg-muted">
        <div className="container mx-auto max-w-6xl">
          <h3 className="text-3xl font-bold text-center text-foreground mb-12">How MentorConnect Works</h3>
          <div className="grid md:grid-cols-3 gap-8">
            <Card>
              <CardHeader>
                <Calendar className="w-10 h-10 text-primary mb-4" />
                <CardTitle>Schedule Meetings</CardTitle>
                <CardDescription>Book one-on-one sessions with professionals in your field of interest</CardDescription>
              </CardHeader>
            </Card>
            <Card>
              <CardHeader>
                <MessageCircle className="w-10 h-10 text-primary mb-4" />
                <CardTitle>Get Real Insights</CardTitle>
                <CardDescription>Learn about daily responsibilities, required skills, and career paths</CardDescription>
              </CardHeader>
            </Card>
            <Card>
              <CardHeader>
                <Star className="w-10 h-10 text-primary mb-4" />
                <CardTitle>Build Your Future</CardTitle>
                <CardDescription>Make informed decisions about your education and career direction</CardDescription>
              </CardHeader>
            </Card>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 px-4">
        <div className="container mx-auto text-center max-w-2xl">
          <h3 className="text-3xl font-bold text-foreground mb-6">Ready to Start Your Journey?</h3>
          <p className="text-lg text-muted-foreground mb-8">
            Join thousands of Romanian students who have found their career direction through MentorConnect.
          </p>
          <Link href="/auth/signup">
            <Button size="lg">Join MentorConnect Today</Button>
          </Link>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-border bg-card py-8 px-4">
        <div className="container mx-auto text-center">
          <p className="text-muted-foreground">
            © 2024 MentorConnect. Helping Romanian students discover their future.
          </p>
        </div>
      </footer>
    </div>
  )
}
